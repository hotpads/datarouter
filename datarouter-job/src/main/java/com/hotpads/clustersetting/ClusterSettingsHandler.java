package com.hotpads.clustersetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.hotpads.datarouter.app.WebAppName;
import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.datarouter.setting.SettingRoot;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.session.CurrentUserInfo;
import com.hotpads.job.dispatcher.DatarouterJobDispatcher;
import com.hotpads.util.core.collections.KeyRangeTool;
import com.hotpads.util.core.collections.Range;

public class ClusterSettingsHandler extends BaseHandler{

	public static final String
			P_name = "name",
			P_serverType = "serverType",
			P_prefix = "prefix",
			P_serverName = "serverName",
			P_application = "application",

			P_scope = "scope",
			P_action = "submitAction",

			V_node = "node",
			V_ancestors = "ancestors",
			V_children = "children",
			V_settings = "settings",
			V_listSettings = "listSettings",
			V_mapListsCustomSettings = "mapListsCustomSettings",
			V_nodeName = "nodeName",
			V_roots = "roots",
			V_currentRootName = "currentRootName",
			V_records = "records",

			V_settingGroups = "settingGroups",
			V_settingGroupsByScope = "settingGroupsByScope",

			URL_settings = DatarouterWebDispatcher.PATH_datarouter + DatarouterJobDispatcher.SETTINGS,
			URL_modify = DatarouterWebDispatcher.PATH_datarouter + DatarouterJobDispatcher.SETTINGS
					+ "?submitAction=browseSettings&name=",

			JSP_editSettings = "/jsp/admin/datarouter/setting/editSettings.jsp",
			JSP_browseSettings = "/jsp/admin/datarouter/setting/browseSettings.jsp",
			JSP_clusterSettingsLog = "/jsp/admin/datarouter/setting/clusterSettingsLog.jsp";

	@Inject
	private SettingRoot settingRoot;
	@Inject
	private ServerType anyServerType;
	@Inject
	private ClusterSettingNodes clusterSettingNodes;
	@Inject
	private WebAppName webAppName;
	@Inject
	private CurrentUserInfo currentUserInfo;

	@Override
	protected Mav handleDefault(){
		return edit();
	}

	/************************** action methods ************************/

	@Handler
	Mav edit(){
		Mav mav = new Mav(JSP_editSettings);
		List<ClusterSetting> settings;
		String prefixString = params.optional(P_prefix).orElse(null);
		Range<ClusterSettingKey> range = KeyRangeTool.forPrefixWithWildcard(prefixString,
				prefix -> new ClusterSettingKey(prefix, null, null, null, null));
		settings = clusterSettingNodes.clusterSetting().stream(range, null).collect(Collectors.toList());
		mav.put(V_settings, settings);
		mav.put("serverTypeOptions", anyServerType.getHtmlSelectOptionsVarNames());
		return mav;
	}

	@Handler
	Mav create(){
		return handleCreateOrUpdate(ClusterSettingLogAction.INSERTED);
	}

	@Handler
	Mav update(){
		return handleCreateOrUpdate(ClusterSettingLogAction.UPDATED);
	}

	@Handler
	Mav viewLog(String name){
		Mav mav = new Mav(JSP_clusterSettingsLog);

		String settingName = name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
		List<ClusterSettingLog> records;

		SettingNode node = getSettingNode(settingName);
		if(node != null){
			//get logs for all settings in specified SettingNode, sorted by date
			List<ClusterSettingLogKey> prefixes = node.getListSettings().stream()
					.map(Setting::getName)
					.map(ClusterSettingLogKey::createPrefix)
					.collect(Collectors.toList());
			records = clusterSettingNodes.clusterSettingLog()
					.streamWithPrefixes(prefixes, null)
					.collect(Collectors.toCollection(ArrayList::new));//need ArrayList for sorting by date below
			records.sort((first, second) -> second.getKey().getCreated().compareTo(first.getKey().getCreated()));

			//whole node/group name plus specification as group node or regular node is displayed
			mav.put(P_name, settingName + (node.isGroup() ? " (group node)" : " (node)"));
			//back button for group nodes goes to parent node, but back button for regular nodes goes back to same node
			mav.put(V_nodeName, node.isGroup() ? DrStringTool.getStringBeforeLastOccurrence('.', settingName)
					: settingName);
		}else{
			//get logs for one specific setting (already sorted by date without intervention)
			ClusterSettingLogKey logPrefix = ClusterSettingLogKey.createPrefix(settingName);
			records = clusterSettingNodes.clusterSettingLog().streamWithPrefix(logPrefix, null)
					.collect(Collectors.toList());

			//whole setting name is displayed
			mav.put(P_name, settingName);
			//back button goes to parent node
			mav.put(V_nodeName, DrStringTool.getStringBeforeLastOccurrence('.', settingName));
		}

		mav.put(V_records, records);
		return mav;
	}


	@Handler
	Mav updateGroup(){
		ClusterSettingScopeValue scopeValue = ClusterSettingScopeValue.parse(params.required(P_scope));
		SettingNode groupNode = getSettingNode(params.required(V_nodeName));

		List<ClusterSettingKey> deleteSettings = new ArrayList<>();
		List<ClusterSetting> createOrUpdateSettings = new ArrayList<>();

		for(Map.Entry<String, String> entry : params.toMap().entrySet()){
			if(P_scope.equals(entry.getKey()) || P_action.equals(entry.getKey()) || V_nodeName.equals(entry.getKey())){
				continue;
			}
			ClusterSettingKey key = scopeValue.toClusterSettingKey(entry.getKey());
			if(entry.getValue().trim().isEmpty()){
				deleteSettings.add(key);
			}else{
				createOrUpdateSettings.add(new ClusterSetting(key, entry.getValue()));
			}
		}

		deleteSettings(deleteSettings);
		createOrUpdateSettings(createOrUpdateSettings, null);

		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify
				+ groupNode.getParentName());
	}

	private Mav handleCreateOrUpdate(ClusterSettingLogAction action){
		ClusterSetting clusterSetting = new ClusterSetting(parseClusterSettingKeyFromParams(),
				params.optional("value").orElse(null));

		createOrUpdateSettings(Arrays.asList(clusterSetting), action);

		Optional<String> nodeName = params.optional("nodeName");
		if(nodeName.isPresent()){
			return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify + nodeName.get()
					+ "#" + clusterSetting.getName());
		}
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_settings);
	}

	private void createOrUpdateSettings(List<ClusterSetting> clusterSettings, ClusterSettingLogAction action){
		List<ClusterSetting> validSettings = new ArrayList<>();
		List<ClusterSettingLog> logs = new ArrayList<>();

		//used to figure out the correct ClusterSettingLogAction for setting groups
		Set<ClusterSettingKey> existingKeys;
		if(action == null){
			List<ClusterSettingKey> keys = clusterSettings.stream()
					.map(ClusterSetting::getKey)
					.collect(Collectors.toList());
			existingKeys = new HashSet<>(clusterSettingNodes.clusterSetting().getKeys(keys, null));
		}else{
			existingKeys = new HashSet<>();
		}

		for(ClusterSetting clusterSetting : clusterSettings){
			Setting<?> setting = settingRoot.getSettingByName(clusterSetting.getName());
			// If setting is null, java does not know about that setting name yet, don't check the type
			if(setting == null || setting.isValid(clusterSetting.getValue())){
				validSettings.add(clusterSetting);
				ClusterSettingLogAction groupAction = existingKeys.contains(clusterSetting.getKey())
						? ClusterSettingLogAction.UPDATED : ClusterSettingLogAction.INSERTED;
				logs.add(new ClusterSettingLog(clusterSetting, action == null ? groupAction : action,
						currentUserInfo.getEmail(request)));
			}
		}

		if(!validSettings.isEmpty()){
			clusterSettingNodes.clusterSetting().putMulti(validSettings, null);
			clusterSettingNodes.clusterSettingLog().putMulti(logs, null);
		}
	}

	@Handler
	Mav delete(){
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		deleteSettings(Arrays.asList(settingKey));

		Optional<String> nodeName = params.optional("nodeName");
		if(nodeName.isPresent()){
			return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify + nodeName.get()
					+ "#" + settingKey.getName());
		}
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_settings);
	}

	@Handler
	Mav deleteGroup(){
		ClusterSettingScopeValue scopeValue = ClusterSettingScopeValue.parse(params.required(P_scope));
		SettingNode groupNode = getSettingNode(params.required(V_nodeName));

		List<ClusterSettingKey> settingKeys = groupNode.getSettings().keySet().stream()
				.map(scopeValue::toClusterSettingKey)
				.collect(Collectors.toList());
		deleteSettings(settingKeys);
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify
				+ groupNode.getParentName());
	}

	private void deleteSettings(Collection<ClusterSettingKey> settingKeys){
		List<ClusterSetting> clusterSettings = clusterSettingNodes.clusterSetting().getMulti(settingKeys, null);
		for(ClusterSetting clusterSetting : clusterSettings){
			ClusterSettingLog log = new ClusterSettingLog(clusterSetting, ClusterSettingLogAction.DELETED,
					currentUserInfo.getEmail(request));
			clusterSettingNodes.clusterSettingLog().put(log, null);
		}
		clusterSettingNodes.clusterSetting().deleteMulti(settingKeys, null);
	}

	@Handler
	Mav browseSettings(){
		Mav mav = new Mav(JSP_browseSettings);

		SettingNode node = getSettingNode(params.optional(P_name).orElse(webAppName + "."));

		mav.put(V_nodeName, node.getName());
		mav.put(V_node, node);
		mav.put(V_ancestors, settingRoot.getDescendants(node.getName()));

		List<SettingNode> groups = new ArrayList<>();
		List<SettingNode> childNodes = new ArrayList<>();
		if(!node.isGroup()){
			node.getListChildren().forEach(n -> {
				if(n.isGroup()){
					groups.add(n);
				}
				childNodes.add(n);
			});
		}
		mav.put(V_children, childNodes);

		//this section does not include the settingGroups (because they're nodes, not settings)
		ArrayList<Setting<?>> settingsList = node.getListSettings();
		Map<String,List<ClusterSetting>> customSettingsByName = new HashMap<>();
		for(Setting<?> setting : settingsList){
			ClusterSettingKey settingKey = new ClusterSettingKey(setting.getName(), null, null, null, null);
			List<ClusterSetting> customSettings = clusterSettingNodes.clusterSetting().stream(
					KeyRangeTool.forPrefix(settingKey), null).collect(Collectors.toList());
			customSettingsByName.put(setting.getName(), customSettings);
		}
		mav.put(V_listSettings, settingsList);
		mav.put(V_mapListsCustomSettings, customSettingsByName);

		//make groups available {groupName: [Setting<?>]}
		//{groupName: {settingScope: { settingName: value }}} < grouped settings grouped by scope
		Map<String, List<Setting<?>>> settingsByGroup = new HashMap<>();
		Map<String, Map<ClusterSettingScopeValue, Map<String, String>>> settingsByGroupAndScope = new TreeMap<>();

		groups.forEach(settingGroup -> {
			List<Setting<?>> settingsInGroup = new ArrayList<>();
			Map<ClusterSettingScopeValue, Map<String, String>> settingGroupByScope = new TreeMap<>();
			settingGroup.getSettings().forEach((settingName, setting) -> {
				settingsInGroup.add(setting);
				ClusterSettingKey settingKey = new ClusterSettingKey(setting.getName(), null, null, null, null);
				List<ClusterSetting> customSettings = clusterSettingNodes.clusterSetting().stream(
						KeyRangeTool.forPrefix(settingKey), null).collect(Collectors.toList());
				customSettingsByName.put(setting.getName(), customSettings);
				customSettings.forEach((customSetting) -> {
					ClusterSettingScopeValue scopeGroup = new ClusterSettingScopeValue(customSetting.getKey());
					if(!settingGroupByScope.containsKey(scopeGroup)){
						settingGroupByScope.put(scopeGroup, new HashMap<>());
					}
					settingGroupByScope.get(scopeGroup).put(settingName, customSetting.getValue());
				});
			});
			settingsByGroup.put(settingGroup.getName(), settingsInGroup);
			settingsByGroupAndScope.put(settingGroup.getName(), settingGroupByScope);
		});
		mav.put(V_settingGroups, settingsByGroup);
		mav.put(V_settingGroupsByScope, settingsByGroupAndScope);

		mav.put(V_currentRootName, node.getName().substring(0, node.getName().indexOf('.')));
		mav.put(V_roots, settingRoot.getRootNodesOrdered());
		mav.put("serverTypeOptions", anyServerType.getHtmlSelectOptionsVarNames());

		return mav;
	}

	private SettingNode getSettingNode(String name){
		name = name.endsWith(".") ? name : name + '.';
		return settingRoot.getNode(name);
	}

	/************************** helper methods ************************/

	protected ClusterSettingKey parseClusterSettingKeyFromParams(){
		String name = params.required(P_name).trim();
		ServerType serverType = anyServerType.fromPersistentString(params.required(P_serverType));
		String serverName = params.optional(P_serverName).orElse("");
		String application = params.optional(P_application).orElse("");
		ClusterSettingScope scope = ClusterSettingScope.fromParams(serverType, serverName, application);
		return new ClusterSettingKey(name, scope, serverType.getPersistentString(), serverName, application);
	}
}
