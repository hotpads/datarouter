package com.hotpads.handler.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.hotpads.WebAppName;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DatarouterDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.setting.ServerType;
import com.hotpads.setting.Setting;
import com.hotpads.setting.cluster.ClusterSetting;
import com.hotpads.setting.cluster.ClusterSettingKey;
import com.hotpads.setting.cluster.ClusterSettingNodes;
import com.hotpads.setting.cluster.ClusterSettingScope;
import com.hotpads.setting.cluster.SettingNode;
import com.hotpads.setting.cluster.SettingRoot;

public class ClusterSettingsHandler extends BaseHandler {

	public static final String
		P_name = "name",
		P_serverType = "serverType",
		P_prefix = "prefix",

		V_node = "node",
		V_ancestors = "ancestors",
		V_children = "children",
		V_settings = "settings",
		V_listSettings = "listSettings",
		V_setting = "setting",
		V_mapListsCustomSettings = "mapListsCustomSettings",
		V_nodeName = "nodeName",
		V_roots = "roots",
		V_currentRootName = "currentRootName",

		URL_settings = DatarouterDispatcher.URL_DATAROUTER + DatarouterDispatcher.SETTING,
		URL_modify = DatarouterDispatcher.URL_DATAROUTER + DatarouterDispatcher.SETTING
					+ "?submitAction=browseSettings&name=",
		JSP_editSettings = "/jsp/admin/datarouter/setting/editSettings.jsp",
		JSP_browseSettings = "/jsp/admin/datarouter/setting/browseSettings.jsp"
		;

	@Inject
	private SettingRoot settingRoot;
	@Inject
	private ServerType anyServerType;
	@Inject
	private ClusterSettingNodes clusterSettingNodes;
	@Inject
	private WebAppName webAppName;

	@Override
	protected Mav handleDefault() {
		return edit();
	}

	/************************** action methods ************************/

	@Handler Mav edit() {
		Mav mav = new Mav(JSP_editSettings);
		List<ClusterSetting> settings;
		String prefix = params.optional(P_prefix, null);
		if(DrStringTool.isEmpty(prefix)) {
//			settings = clusterSettingNode.getAll(null); deprecated
			settings = DrListTool.createArrayList(clusterSettingNodes.clusterSetting().scan(null, null));
		} else {
			ClusterSettingKey settingPrefix = new ClusterSettingKey(prefix, null, null, null, null);
			settings = clusterSettingNodes.clusterSetting().getWithPrefix(settingPrefix, true, null);
		}
		mav.put(V_settings, settings);
		mav.put("serverTypeOptions", anyServerType.getHTMLSelectOptionsVarNames());
		return mav;
	}

	@Handler Mav create() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		String value = params.optional("value", null);
		ClusterSetting setting = new ClusterSetting(settingKey, value);
		clusterSettingNodes.clusterSetting().put(setting, null);
		String nodeName = params.optional("nodeName", null);
		if(nodeName != null){
			return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify + nodeName + "#"
					+ settingKey.getName().replaceAll("\\.", "_"));
		}
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_settings);
	}

	@Handler Mav update() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		String value = params.optional("value", null);
		ClusterSetting setting = new ClusterSetting(settingKey, value);
		clusterSettingNodes.clusterSetting().put(setting, null);
		String nodeName = params.optional("nodeName", null);
		if(nodeName != null){
			return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify + nodeName + "#"
					+ settingKey.getName().replaceAll("\\.", "_"));
		}
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_settings);
	}

	@Handler Mav modify() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		String value = params.optional("value", null);
		ClusterSetting setting = new ClusterSetting(settingKey, value);
		clusterSettingNodes.clusterSetting().put(setting, null);
		String nodeName = params.required("nodeName");
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify+nodeName);
	}

	@Handler Mav delete() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		clusterSettingNodes.clusterSetting().delete(settingKey, null);
		String nodeName = params.optional("nodeName", null);
		if(nodeName != null){
			return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify + nodeName + "#"
					+ settingKey.getName().replaceAll("\\.", "_"));
		}
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_settings);
	}

	@Handler Mav browseSettings(){
		Mav mav = new Mav(JSP_browseSettings);
		String nodeName = params.optional(P_name, webAppName + ".");
		if(!nodeName.endsWith(".")){
			nodeName = nodeName + ".";
		}
		mav.put(V_nodeName, nodeName);

		SettingNode node = settingRoot.getNodeByName(nodeName);
		mav.put(V_node, node);
		mav.put(V_ancestors, settingRoot.getDescendanceByName(nodeName));
		mav.put(V_children, node.getListChildren());
		ArrayList<Setting<?>> settingsList = node.getListSettings();
		Map<String,List<ClusterSetting>> mapListsCustom = new HashMap<>();
		for(Setting<?> setting : settingsList){
			ClusterSettingKey settingKey = new ClusterSettingKey(setting.getName(), null, null, null, null);
			List<ClusterSetting> clusterSettings = clusterSettingNodes.clusterSetting().getWithPrefix(settingKey,
					false, null);
			mapListsCustom.put(setting.getName(), clusterSettings);
		}
		mav.put(V_listSettings, settingsList);
		mav.put(V_mapListsCustomSettings, mapListsCustom);
		mav.put(V_currentRootName, node.getName().substring(0, node.getName().indexOf('.')));
		mav.put(V_roots, settingRoot.getRootNodes());
		mav.put("serverTypeOptions", anyServerType.getHTMLSelectOptionsVarNames());
		return mav;
	}

	/************************** helper methods ************************/

	protected ClusterSettingKey parseClusterSettingKeyFromParams() {
		String name = params.required(P_name);
		ServerType serverType = anyServerType.fromPersistentString(params.required(P_serverType));
		String instance = params.optional("instance", "");
		String application = params.optional("application", "");
		ClusterSettingScope scope = ClusterSettingScope.fromParams(serverType, instance, application);
		return new ClusterSettingKey(name, scope, serverType.getPersistentString(), instance, application);
	}

}
