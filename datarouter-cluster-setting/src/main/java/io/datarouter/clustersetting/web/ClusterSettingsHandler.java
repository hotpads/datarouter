/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.clustersetting.web;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.clustersetting.ClusterSettingLogAction;
import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.clustersetting.config.DatarouterClusterSettingFiles;
import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.config.DatarouterClusterSettingRoot;
import io.datarouter.clustersetting.service.ClusterSettingChangelogService;
import io.datarouter.clustersetting.service.ClusterSettingEmailService;
import io.datarouter.clustersetting.service.ClusterSettingSearchService;
import io.datarouter.clustersetting.service.ClusterSettingSearchService.SettingNameMatchResult;
import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.web.dto.ClusterSettingJspDto;
import io.datarouter.clustersetting.web.dto.SettingJspDto;
import io.datarouter.clustersetting.web.dto.SettingNodeJspDto;
import io.datarouter.clustersetting.web.log.ClusterSettingLogHandler.ClusterSettingLogLinks;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.DatarouterServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.SettingCategory;
import io.datarouter.storage.setting.SettingCategory.SimpleSettingCategory;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.SettingRoot.SettingRootFinder;
import io.datarouter.storage.setting.cached.CachedClusterSettingTags;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;

public class ClusterSettingsHandler extends BaseHandler{

	private static final int CLUSTER_SETTING_SEARCH_RESULTS = 10;

	@Inject
	private SettingRootFinder settingRootFinder;
	@Inject
	private ServerTypes serverTypes;
	@Inject
	private DatarouterClusterSettingDao clusterSettingDao;
	@Inject
	private DatarouterClusterSettingLogDao clusterSettingLogDao;
	@Inject
	private DatarouterClusterSettingFiles files;
	@Inject
	private ClusterSettingService clusterSettingService;
	@Inject
	private ClusterSettingSearchService clusterSettingSearchService;
	@Inject
	private DatarouterClusterSettingRoot settings;
	@Inject
	private DatarouterServerTypeDetector datarouterServerTypeDetector;
	@Inject
	private CachedClusterSettingTags cachedClusterSettingTags;
	@Inject
	private DatarouterClusterSettingPaths paths;
	@Inject
	private ClusterSettingLogLinks clusterSettingLogLinks;
	@Inject
	private ClusterSettingChangelogService clusterSettingChangelogService;
	@Inject
	private ClusterSettingEmailService clusterSettingEmailService;

	@Handler
	public List<String> roots(){
		return Scanner.of(settingRootFinder.getRootNodesSortedByShortName())
				.map(SettingNode::getShortName)
				.list();
	}

	@Handler
	public Boolean isRecognizedRoot(String name){
		return settingRootFinder.isRecognizedRootName(name);
	}

	@Handler
	public ClusterSettingActionResultJson create(){
		return putSettingFromParams();
	}

	@Handler
	public ClusterSettingActionResultJson update(){
		return putSettingFromParams();
	}

	@Handler
	public ClusterSettingActionResultJson updateSettingTags(String values){
		cachedClusterSettingTags.writeToFile(values);
		return new ClusterSettingActionResultJson(ClusterSettingLogAction.UPDATED).markSuccess();
	}

	@Handler
	public ClusterSettingActionResultJson delete(){
		ClusterSettingLogAction action = ClusterSettingLogAction.DELETED;
		var result = new ClusterSettingActionResultJson(action);
		ClusterSettingKey clusterSettingKey = parseClusterSettingKeyFromParams();
		Optional<String> comment = parseCommentFromParams();
		String changedBy = getRequestorsUsername();
		ClusterSetting clusterSetting = clusterSettingDao.get(clusterSettingKey);
		var clusterSettingLog = new ClusterSettingLog(clusterSetting, action, changedBy, comment.orElse(null));
		clusterSettingDao.delete(clusterSettingKey);
		clusterSettingLogDao.put(clusterSettingLog);
		String oldValue = clusterSetting.getValue();
		clusterSettingEmailService.sendEmail(
				clusterSettingLog,
				oldValue,
				getSessionInfo().findNonEmptyUsername(),
				getUserZoneId());
		clusterSettingChangelogService.recordChangelog(
				clusterSettingLog.getKey().getName(),
				clusterSettingLog.getAction().persistentString,
				clusterSettingLog.getChangedBy(),
				comment);
		return result.markSuccess();
	}

	@Handler
	public Mav browseSettings(Optional<String> name){
		Mav mav = new Mav(files.jsp.admin.datarouter.setting.browseSettingsJsp);
		String requestedNodeName = name.orElse(settingRootFinder.getName());
		mav.put("nodeName", name.orElse(""));

		Map<SimpleSettingCategory,Set<SettingNodeJspDto>> categoryMap = new LinkedHashMap<>();
		for(Entry<SimpleSettingCategory,Set<SettingRoot>> entry : settingRootFinder.getRootNodesByCategory()
				.entrySet()){
			var nodes = entry.getValue().stream()
					.map(SettingNodeJspDto::new)
					.collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(
							SettingNodeJspDto::getShortName))));
			categoryMap.put(entry.getKey(), nodes);
		}
		Optional<String> currentCategory = settingRootFinder.getRootNodesByCategory().entrySet().stream()
				.findFirst()
				.map(Entry::getKey)
				.map(SimpleSettingCategory::getDisplay);
		mav.put("currentCategory", currentCategory.orElse(""));
		mav.put("categoryMap", categoryMap);

		mav.put("serverTypeOptions", serverTypes.getHtmlSelectOptionsVarNames());
		Optional<SettingNode> node = getSettingNode(requestedNodeName);

		//if the node was not found, get the most recent ancestor to search its settings list
		boolean trySearchingForSpecificSetting = node.isEmpty();
		if(trySearchingForSpecificSetting){
			node = getMostRecentAncestorSettingNode(requestedNodeName);
		}

		if(name.isPresent()){
			mav.put("exists", node.isPresent() || settings.getSettingByName(name.orElseThrow()).isPresent());
			long numDots = StringTool.scanCharacters(name.orElseThrow())
					.include(ch -> ch == '.')
					.count();
			boolean isRoot = name.orElseThrow().endsWith(".") && numDots == 1;
			mav.put("isRoot", isRoot);
			boolean isNode = name.orElseThrow().endsWith(".") && numDots > 1;
			mav.put("isNode", isNode);
			boolean isSetting = !name.orElseThrow().endsWith(".");
			mav.put("isSetting", isSetting);
			if(isSetting){
				mav.put("settingShortName", name.orElseThrow().substring(
						name.orElseThrow().lastIndexOf('.') + 1,
						name.orElseThrow().length()));
			}
			mav.put("numNodeLogs", clusterSettingLogDao.scanWithWildcardPrefix(name.orElseThrow()).count());
			mav.put("nodeLogHref", clusterSettingLogLinks.node(name.orElseThrow()));
		}
		if(node.isEmpty()){
			return mav;
		}

		node.ifPresent(settingNode -> {
			String rootName = StringTool.getStringBeforeFirstOccurrence('.', settingNode.getName());
			settingRootFinder.getRootNodesSortedByShortName().stream()
					.filter(rootNode -> rootNode.getShortName().equals(rootName))
					.findFirst()
					.map(SettingRoot.class::cast)
					.map(SettingRoot::getSettingCategory)
					.map(SettingCategory::getDisplay)
					.ifPresent(category -> mav.put("currentCategory", category));
			mav.put("currentRootName", rootName);
		});

		mav.put("ancestors", Scanner.of(settingRootFinder.getDescendants(node.get().getName()))
				.map(SettingNodeJspDto::new)
				.list());
		mav.put("currentRootName", node.get().getName().substring(0, node.get().getName().indexOf('.')));
		mav.put("children", Scanner.of(node.get().getListChildren()).map(SettingNodeJspDto::new).list());

		List<CachedSetting<?>> settingsList = node.get().getListSettings();
		if(trySearchingForSpecificSetting){
			settingsList = settingsList.stream()
					.filter(setting -> setting.getName().equals(requestedNodeName))
					.collect(Collectors.toList());
		}

		Map<String,List<ClusterSettingJspDto>> customSettingsByName = new HashMap<>();
		for(CachedSetting<?> setting : settingsList){
			ClusterSettingKey settingKey = new ClusterSettingKey(setting.getName(), null, null, null);
			List<ClusterSetting> settingsInDb = clusterSettingDao.scanWithPrefix(settingKey).list();
			Optional<ClusterSetting> mostSpecificSetting = clusterSettingService.getMostSpecificClusterSetting(
					settingsInDb);
			boolean isActive = setting.getMostSpecificDatabeanValue().isPresent();
			Scanner.of(settingsInDb)
					.map(settingFromDb -> {
						boolean isWinner = mostSpecificSetting.isPresent() && settingFromDb.equals(mostSpecificSetting
								.get());
						ClusterSettingJspDto jspDto = new ClusterSettingJspDto(settingFromDb, isActive, isWinner);
						return jspDto;
					})
					.flush(customSettings -> customSettingsByName.put(setting.getName(), customSettings));
		}
		mav.put("listSettings", Scanner.of(settingsList)
				.map(setting -> new SettingJspDto(
						setting,
						clusterSettingLogDao.scanWithWildcardPrefix(setting.getName()).count(),
						clusterSettingLogLinks.setting(setting.getName())))
				.list());
		mav.put("mightBeDevelopment", datarouterServerTypeDetector.mightBeDevelopment());
		mav.put("mapListsCustomSettings", customSettingsByName);

		return mav;
	}

	@Handler
	public List<SettingNameMatchResult> searchSettingNames(String term){
		return clusterSettingSearchService.searchSettingNames(term, CLUSTER_SETTING_SEARCH_RESULTS);
	}

	/*-------------------------------- private -------------------------------*/

	private ClusterSettingActionResultJson putSettingFromParams(){
		ClusterSettingKey clusterSettingKey = parseClusterSettingKeyFromParams();
		Optional<String> comment = parseCommentFromParams();
		String value = params.optional("value").orElse(null);
		var newClusterSetting = new ClusterSetting(clusterSettingKey, value);
		Optional<CachedSetting<?>> setting = settingRootFinder.getSettingByName(newClusterSetting.getName());
		ClusterSettingLogAction action;
		Optional<ClusterSetting> dbSetting = clusterSettingDao.find(clusterSettingKey);
		if(dbSetting.isPresent()){
			action = ClusterSettingLogAction.UPDATED;
		}else{
			action = ClusterSettingLogAction.INSERTED;
		}
		var result = new ClusterSettingActionResultJson(action);
		if(setting.isPresent() && !setting.get().isValid(newClusterSetting.getValue())){
			String badNewValue = newClusterSetting.getValue();
			String error = "Invalid value detected, setting did not accept new value: \"" + badNewValue + "\"";
			return result.markError(error);
		}
		String oldValue = dbSetting
				.map(ClusterSetting::getValue)
				.orElse("?");
		String changedBy = getRequestorsUsername();
		var log = new ClusterSettingLog(newClusterSetting, action, changedBy, comment.orElse(null));
		clusterSettingDao.put(newClusterSetting);
		clusterSettingLogDao.put(log);
		clusterSettingEmailService.sendEmail(log, oldValue, getSessionInfo().findNonEmptyUsername(), getUserZoneId());
		clusterSettingChangelogService.recordChangelog(
				log.getKey().getName(),
				log.getAction().persistentString,
				log.getChangedBy(),
				Optional.ofNullable(log.getComment()));
		return result.markSuccess();
	}

	public static String normalizeSettingNodeName(String name){
		return name.endsWith(".") ? name : name + '.';
	}

	private Optional<SettingNode> getSettingNode(String name){
		return settingRootFinder.getNode(normalizeSettingNodeName(name));
	}

	private Optional<SettingNode> getMostRecentAncestorSettingNode(String name){
		return settingRootFinder.getMostRecentAncestorNode(normalizeSettingNodeName(name));
	}

	private String getRequestorsUsername(){
		return getSessionInfo().getNonEmptyUsernameOrElse("");
	}

	private Optional<String> parseCommentFromParams(){
		return params.optional("comment")
				.filter(StringTool::notNullNorEmptyNorWhitespace);
	}

	private ClusterSettingKey parseClusterSettingKeyFromParams(){
		String name = params.required("name").trim();
		// allow unrecognized serverType
		String serverTypePersistentString = params.required("serverType");
		String serverName = params.optional("serverName").orElse("");
		ClusterSettingScope scope = ClusterSettingScope.fromParams(serverTypePersistentString, serverName);
		return new ClusterSettingKey(name, scope, serverTypePersistentString, serverName);
	}

	protected static class ClusterSettingActionResultJson{

		public final ClusterSettingLogAction clusterSettingLogAction;
		public boolean success = false;
		public String error;

		public ClusterSettingActionResultJson(ClusterSettingLogAction clusterSettingLogAction){
			this.clusterSettingLogAction = clusterSettingLogAction;
		}

		public ClusterSettingActionResultJson markSuccess(){
			this.success = true;
			return this;
		}

		public ClusterSettingActionResultJson markError(String error){
			this.success = false;
			this.error = error;
			return this;
		}

	}

}
