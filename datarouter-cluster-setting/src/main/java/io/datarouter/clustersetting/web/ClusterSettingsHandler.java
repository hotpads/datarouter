/**
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

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.config.DatarouterClusterSettingFiles;
import io.datarouter.clustersetting.config.DatarouterClusterSettingRoot;
import io.datarouter.clustersetting.service.ClusterSettingSearchService;
import io.datarouter.clustersetting.service.ClusterSettingSearchService.SettingNameMatchResult;
import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLogByReversedCreatedMsKey;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLogKey;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.web.dto.ClusterSettingJspDto;
import io.datarouter.clustersetting.web.dto.ClusterSettingLogJspDto;
import io.datarouter.clustersetting.web.dto.SettingJspDto;
import io.datarouter.clustersetting.web.dto.SettingNodeJspDto;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.DatarouterServerTypeDetector;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.DatarouterSettingTag;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingCategory;
import io.datarouter.storage.setting.SettingCategory.SimpleSettingCategory;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.SettingRoot.SettingRootFinder;
import io.datarouter.storage.setting.cached.CachedClusterSettingTags;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.DateTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.email.DatarouterEmailLinkBuilder;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.optional.OptionalBoolean;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.email.J2HtmlDatarouterEmailBuilder;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.email.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class ClusterSettingsHandler extends BaseHandler{

	private static final int CLUSTER_SETTING_LOGS_PAGE_SIZE = 50;
	private static final int CLUSTER_SETTING_SEARCH_RESULTS = 10;

	@Inject
	private DatarouterHtmlEmailService datarouterHtmlEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
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
	private DatarouterAdministratorEmailService datarouterAdministratorEmailService;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterWebPaths datarouterWebPaths;
	@Inject
	private ClusterSettingSearchService clusterSettingSearchService;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private DatarouterClusterSettingRoot settings;
	@Inject
	private DatarouterServerTypeDetector datarouterServerTypeDetector;
	@Inject
	private CachedClusterSettingTags cachedClusterSettingTags;

	@Handler
	public Mav customSettings(OptionalString prefix){
		Mav mav = new Mav(files.jsp.admin.datarouter.setting.editSettingsJsp);
		mav.put("serverTypeOptions", serverTypes.getHtmlSelectOptionsVarNames());
		mav.put("validities", buildLegend());
		clusterSettingService.scanClusterSettingAndValidityWithPrefix(prefix.orElse(null))
				.flush(settings -> mav.put("rows", settings));
		boolean mightBeDevelopment = datarouterServerTypeDetector.mightBeDevelopment();
		mav.put("mightBeDevelopment", mightBeDevelopment);
		if(mightBeDevelopment){
			mav.put("settingTagFilePath", CachedClusterSettingTags.getConfigFilePath());
			List<DatarouterSettingTag> tags = cachedClusterSettingTags.get();
			mav.put("settingTagValues", tags.stream().map(DatarouterSettingTag::getPersistentString).collect(Collectors
					.joining(",")));
		}
		return mav;
	}

	@Handler
	public List<String> roots(){
		return Scanner.of(settingRootFinder.getRootNodesSortedByShortName()).map(SettingNode::getShortName).list();
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
		String comment = parseCommentFromParams();
		String changedBy = getRequestorsUsername();
		ClusterSetting clusterSetting = clusterSettingDao.get(clusterSettingKey);
		var clusterSettingLog = new ClusterSettingLog(clusterSetting, action, changedBy, comment);
		clusterSettingDao.delete(clusterSettingKey);
		clusterSettingLogDao.put(clusterSettingLog);
		String oldValue = clusterSetting.getValue();
		sendEmail(clusterSettingLog, oldValue);
		recordChangelog(clusterSettingLog.getKey().getName(), clusterSettingLog.getAction().getPersistentString(),
				clusterSettingLog.getChangedBy(), comment);
		return result.markSuccess();
	}

	@Handler
	public Mav logsForName(String name){
		Mav mav = new Mav(files.jsp.admin.datarouter.setting.clusterSettingsLogJsp);
		mav.put("showingAllSettings", false);
		String settingName = name.endsWith(".") ? StringTool.getStringBeforeLastOccurrence('.', name) : name;
		mav.put("nameParts", settingName.split("\\."));
		Optional<SettingNode> node = getSettingNode(settingName);
		mav.put("showingNodeSettings", node.isPresent());
		Scanner<ClusterSettingLog> logScanner;
		if(node.isPresent()){
			// logs for node and its descendants
			List<ClusterSettingLogKey> prefixes = node.get().getListSettings().stream()
					.map(Setting::getName)
					.map(ClusterSettingLogKey::createPrefix)
					.collect(Collectors.toList());
			logScanner = clusterSettingLogDao.scanWithPrefixes(prefixes)
					.sorted(Comparator.comparing((ClusterSettingLog log) -> log.getKey().getCreated()).reversed());
		}else{
			// logs for single setting
			ClusterSettingLogKey prefix = ClusterSettingLogKey.createPrefix(settingName);
			logScanner = clusterSettingLogDao.scanWithPrefix(prefix);
		}
		logScanner
				.map(setting -> new ClusterSettingLogJspDto(setting, datarouterService.getZoneId()))
				.flush(logs -> mav.put("logs", logs));
		return mav;
	}

	@Handler
	public Mav logsForAll(OptionalString explicitStartIso, OptionalBoolean inclusiveStart){
		Mav mav = new Mav(files.jsp.admin.datarouter.setting.clusterSettingsLogJsp);
		mav.put("showingAllSettings", true);
		long startCreatedMs = explicitStartIso
				.map(isoDate -> LocalDateTime.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
				.map(localDate -> localDate.atZone(ZoneId.systemDefault()))
				.map(ZonedDateTime::toInstant)
				.map(Instant::toEpochMilli)
				.orElseGet(System::currentTimeMillis);
		long reverseStartCreatedMs = Long.MAX_VALUE - startCreatedMs;
		Range<ClusterSettingLogByReversedCreatedMsKey> range = new Range<>(
				new ClusterSettingLogByReversedCreatedMsKey(reverseStartCreatedMs, null), inclusiveStart.orElse(false));
		clusterSettingLogDao
				.scanByReversedCreatedMs(range, CLUSTER_SETTING_LOGS_PAGE_SIZE)
				.map(setting -> new ClusterSettingLogJspDto(setting, datarouterService.getZoneId()))
				.flush(logs -> mav.put("logs", logs))
				.flush(logs -> mav.put("hasNextPage", logs.size() == CLUSTER_SETTING_LOGS_PAGE_SIZE));
		mav.put("hasPreviousPage", explicitStartIso.isPresent());
		return mav;
	}

	@Handler
	public Mav browseSettings(OptionalString name){
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
		mav.put("listSettings", Scanner.of(settingsList).map(SettingJspDto::new).list());
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
		String comment = parseCommentFromParams();
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
		var clusterSettingLog = new ClusterSettingLog(newClusterSetting, action, changedBy, comment);
		clusterSettingDao.put(newClusterSetting);
		clusterSettingLogDao.put(clusterSettingLog);
		sendEmail(clusterSettingLog, oldValue);
		recordChangelog(clusterSettingLog.getKey().getName(), clusterSettingLog.getAction().getPersistentString(),
				clusterSettingLog.getChangedBy(), clusterSettingLog.getComment());
		return result.markSuccess();
	}

	private String normalizeSettingNodeName(String name){
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

	private String parseCommentFromParams(){
		return params.optional("comment")
				.filter(StringTool::notNullNorEmptyNorWhitespace)
				.orElse(null);
	}

	private ClusterSettingKey parseClusterSettingKeyFromParams(){
		String name = params.required("name").trim();
		// allow unrecognized serverType
		String serverTypePersistentString = params.required("serverType");
		String serverName = params.optional("serverName").orElse("");
		ClusterSettingScope scope = ClusterSettingScope.fromParams(serverTypePersistentString, serverName);
		return new ClusterSettingKey(name, scope, serverTypePersistentString, serverName);
	}

	private void sendEmail(ClusterSettingLog log, String oldValue){
		String from = datarouterProperties.getAdministratorEmail();
		String to = datarouterAdministratorEmailService.getAdministratorEmailAddressesCsv() + ","
				+ getSessionInfo().getNonEmptyUsernameOrElse("");
		String title = "Setting Update";
		String primaryHref = completeLink(datarouterHtmlEmailService.startLinkBuilder(), log)
				.build();
		boolean displayValue = !settings.isExcludedOldSettingString(log.getKey().getName());
		J2HtmlDatarouterEmailBuilder emailBuilder = datarouterHtmlEmailService.startEmailBuilder()
				.withTitle(title)
				.withTitleHref(primaryHref)
				.withContent(new ClusterSettingChangeEmailContent(log, oldValue, displayValue).build());
		datarouterHtmlEmailService.trySendJ2Html(from, to, emailBuilder);
	}

	private static String buildLegend(){
		var legend = new J2HtmlLegendTable()
					.withClass("table table-sm my-4 border");
		ClusterSettingValidity.stream()
				.forEach(validity -> legend.withEntry(
						validity.persistentString,
						validity.description,
						validity.color));
		return legend.build()
				.renderFormatted();
	}

	private DatarouterEmailLinkBuilder completeLink(DatarouterEmailLinkBuilder linkBuilder, ClusterSettingLog log){
		return linkBuilder
				.withLocalPath(datarouterWebPaths.datarouter.settings)
				.withParam("submitAction", "browseSettings")
				.withParam("name", log.getKey().getName());
	}

	private void recordChangelog(String name, String action, String username, String comment){
		changelogRecorder.record("ClusterSetting", name, action, username, comment);
	}

	private class ClusterSettingChangeEmailContent{
		private final ClusterSettingLog log;
		private final String oldValue;
		private final boolean displayValue;

		public ClusterSettingChangeEmailContent(ClusterSettingLog log, String oldValue, boolean displayValue){
			this.log = log;
			this.oldValue = oldValue;
			this.displayValue = displayValue;
		}

		private ContainerTag build(){
			List<Pair<String,DomContent>> kvs = new ArrayList<>();
			kvs.add(new Pair<>("environment", makeText(datarouterProperties.getEnvironment())));
			kvs.add(new Pair<>("service", makeText(datarouterService.getServiceName())));
			kvs.add(new Pair<>("host", makeText(datarouterProperties.getServerName())));
			kvs.add(new Pair<>("user", makeText(log.getChangedBy())));
			kvs.add(new Pair<>("action", makeText(log.getAction().getPersistentString())));
			kvs.add(new Pair<>("setting", makeClusterSettingLogLink()));
			String timestamp = DateTool.formatReversedLongMsWithZone(log.getKey().getReverseCreatedMs(),
					datarouterService.getZoneId());
			kvs.add(new Pair<>("timestamp", makeText(timestamp)));
			if(ObjectTool.notEquals(ServerType.UNKNOWN.getPersistentString(), log.getServerType())){
				kvs.add(new Pair<>("serverType", makeText(log.getServerType())));
			}
			if(StringTool.notEmpty(log.getServerName())){
				kvs.add(new Pair<>("serverName", makeText(log.getServerName())));
			}
			if(displayValue){
				if(ClusterSettingLogAction.INSERTED != log.getAction()){
					kvs.add(new Pair<>("old value", makeText(oldValue)));
				}
				if(ClusterSettingLogAction.DELETED != log.getAction()){
					kvs.add(new Pair<>("new value", makeText(log.getValue())));
				}
			}
			String comment = StringTool.notNullNorEmptyNorWhitespace(log.getComment())
					? log.getComment()
					: "No comment provided";
			kvs.add(new Pair<>("comment", makeText(comment)));

			return new J2HtmlEmailTable<Pair<String,DomContent>>()
					.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
					.withColumn(new J2HtmlEmailTableColumn<>(null, Pair::getRight))
					.build(kvs);
		}

		private DomContent makeDivBoldRight(String text){
			return div(text)
					.withStyle("font-weight:bold;text-align:right;");
		}

		private DomContent makeText(String text){
			return text(text);
		}

		private DomContent makeClusterSettingLogLink(){
			return a(log.getKey().getName())
					.withHref(completeLink(datarouterHtmlEmailService.startLinkBuilder(), log)
							.build());
		}

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
