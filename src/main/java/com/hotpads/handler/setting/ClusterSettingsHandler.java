package com.hotpads.handler.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DataRouterDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.setting.DatarouterServerType;
import com.hotpads.setting.Setting;
import com.hotpads.setting.cluster.ClusterSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder.clusterSettingNode;
import com.hotpads.setting.cluster.ClusterSettingKey;
import com.hotpads.setting.cluster.ClusterSettingScope;
import com.hotpads.setting.cluster.SettingNode;
import com.hotpads.setting.cluster.SettingRoot;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.StringTool;

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
		
		URL_settings = DataRouterDispatcher.URL_DATAROUTER + DataRouterDispatcher.SETTING,
		URL_modify = DataRouterDispatcher.URL_DATAROUTER + DataRouterDispatcher.SETTING + "?submitAction=browseSettings&name=",
		JSP_editSettings = "/jsp/admin/datarouter/setting/editSettings.jsp",
		JSP_browseSettings = "/jsp/admin/datarouter/setting/browseSettings.jsp";

	private SettingRoot settingRegister;
	private DatarouterServerType datarouterServerTypeTool;
	private SortedMapStorageNode<ClusterSettingKey, ClusterSetting> clusterSettingNode;
	
	@Inject
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ClusterSettingsHandler(SettingRoot settingRegister, DatarouterServerType datarouterServerTypeTool, @clusterSettingNode SortedMapStorageNode clusterSettingNode) {
		this.settingRegister = settingRegister;
		this.datarouterServerTypeTool = datarouterServerTypeTool;
		this.clusterSettingNode = clusterSettingNode;
	}
	
	@Override
	protected Mav handleDefault() {
		return edit();
	}
	
	/************************** action methods ************************/
	
	@Handler Mav edit() {
		Mav mav = new Mav(JSP_editSettings);
		List<ClusterSetting> settings;
		String prefix = params.optional(P_prefix, null);
		if(StringTool.isEmpty(prefix)) {
//			settings = clusterSettingNode.getAll(null); deprecated
			settings = ListTool.createArrayList(clusterSettingNode.scan(null, true, null, false, null));
		} else {
			ClusterSettingKey settingPrefix = new ClusterSettingKey(prefix, null, null, null, null);
			settings = clusterSettingNode.getWithPrefix(settingPrefix, true, null);
		}
		mav.put(V_settings, settings);
		mav.put("serverTypeOptions", datarouterServerTypeTool.getHTMLSelectOptionsVarNames());
		return mav;
	}
	
	@Handler Mav create() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		String value = params.optional("value", null);
		ClusterSetting setting = new ClusterSetting(settingKey, value);
		clusterSettingNode.put(setting, null);
		String nodeName = params.optional("nodeName", null);
		if(nodeName != null){
			return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify+nodeName+"#"+settingKey.getName().replaceAll("\\.", "_"));
		}
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_settings);
	}
	
	@Handler Mav update() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		String value = params.optional("value", null);
		ClusterSetting setting = new ClusterSetting(settingKey, value);
		clusterSettingNode.put(setting, null);
		String nodeName = params.optional("nodeName", null);
		if(nodeName != null){
			return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify+nodeName+"#"+settingKey.getName().replaceAll("\\.", "_"));
		}
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_settings);
	}
	
	@Handler Mav modify() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		String value = params.optional("value", null);
		ClusterSetting setting = new ClusterSetting(settingKey, value);
		clusterSettingNode.put(setting, null);
		String nodeName = params.required("nodeName");
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify+nodeName);
	}
	
	@Handler Mav delete() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		clusterSettingNode.delete(settingKey, null);
		String nodeName = params.optional("nodeName", null);
		if(nodeName != null){
			return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_modify+nodeName+"#"+settingKey.getName().replaceAll("\\.", "_"));
		}
		return new Mav(Mav.REDIRECT + request.getServletContext().getContextPath() + URL_settings);
	}

	@Handler Mav browseSettings(){
		Mav mav = new Mav(JSP_browseSettings);
		String context = request.getServletContext().getContextPath().replace("/", "");
		String nodeName = params.optional(P_name, context + ".");
		mav.put(V_nodeName, nodeName);
		
		SettingNode node = settingRegister.getNodeByName(nodeName);
		mav.put(V_node, node);
		mav.put(V_ancestors, settingRegister.getDescendanceByName(nodeName));
		mav.put(V_children, node.getListChildren());
		ArrayList<Setting<?>> settingsList = (ArrayList<Setting<?>>)node
				.getListSettings();
		Map<String,List<ClusterSetting>> mapListsCustom = MapTool.createHashMap();
		for(Setting<?> setting : settingsList){
			ClusterSettingKey settingKey = new ClusterSettingKey(setting.getName(), null, null, null, null);
			List<ClusterSetting> clusterSettings = clusterSettingNode.getWithPrefix(settingKey, true, null);
			mapListsCustom.put(setting.getName(), clusterSettings);
		}
		mav.put(V_listSettings, settingsList);
		mav.put(V_mapListsCustomSettings, mapListsCustom);
		mav.put(V_roots, settingRegister.getRootNodes());
		mav.put("serverTypeOptions", datarouterServerTypeTool.getHTMLSelectOptionsVarNames());
		return mav;
	}
	
	/************************** helper methods ************************/
	
	protected ClusterSettingKey parseClusterSettingKeyFromParams() {
		String name = params.required(P_name);
		DatarouterServerType serverType = datarouterServerTypeTool.fromPersistentStringStatic(params.required(P_serverType));
		String instance = params.optional("instance", "");
		String application = params.optional("application", "");
		ClusterSettingScope scope = ClusterSettingScope.fromParams(serverType, instance, application);
		return new ClusterSettingKey(name, scope, serverType.getPersistentString(), instance, application);
	}

}
