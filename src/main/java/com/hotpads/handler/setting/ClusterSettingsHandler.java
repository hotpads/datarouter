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
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.ClusterSettingFinder.clusterSettingNode;
import com.hotpads.setting.cluster.ClusterSettingKey;
import com.hotpads.setting.cluster.ClusterSettingScope;
import com.hotpads.setting.cluster.SettingNode;
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
		
		URL_settings = "/job" + DataRouterDispatcher.URL_DATAROUTER + DataRouterDispatcher.SETTING,
		URL_modify = "/job" + DataRouterDispatcher.URL_DATAROUTER + DataRouterDispatcher.SETTING + "?submitAction=browseSettings&name=",
		JSP_editSettings = "/jsp/admin/datarouter/setting/editSettings.jsp",
		JSP_browseSettings = "/jsp/admin/datarouter/setting/browseSettings.jsp",
		JSP_detailSetting = "/jsp/admin/datarouter/setting/detailSetting.jsp";
	

	
	@Inject
	protected SettingNode settingNode;
	@Inject
	protected ClusterSettingFinder finder;
	@Inject
	protected DatarouterServerType datarouterServerTypeTool;
	protected  SortedMapStorageNode<ClusterSettingKey, ClusterSetting> clusterSettingNode;
	
	@Inject
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ClusterSettingsHandler(@clusterSettingNode SortedMapStorageNode clusterSettingNode) {
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
		return new Mav(Mav.REDIRECT+URL_settings);
	}
	
	@Handler Mav update() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		String value = params.optional("value", null);
		ClusterSetting setting = new ClusterSetting(settingKey, value);
		clusterSettingNode.put(setting, null);
		return new Mav(Mav.REDIRECT+URL_settings);
	}
	
	@Handler Mav modify() {
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		String nodeName = params.required("nodeName");
		//String name = params.required("name");
		String value = params.optional("value", null);
		ClusterSetting setting = new ClusterSetting(settingKey, value);
		clusterSettingNode.put(setting, null);
		return new Mav(Mav.REDIRECT+URL_modify+nodeName);
	}
	
	@Handler Mav delete() {
		String nodeName = params.optional("nodeName", null);
		ClusterSettingKey settingKey = parseClusterSettingKeyFromParams();
		clusterSettingNode.delete(settingKey, null);
		if(nodeName != null){
			return new Mav(Mav.REDIRECT+URL_modify+nodeName);
		}
		return new Mav(Mav.REDIRECT+URL_settings);
	}
	
	@Handler Mav detailSetting(){
		Mav mav = new Mav(JSP_detailSetting);
		String settingName = params.required(P_name);
		Setting<?> setting = settingNode.getDescendantSettingByName(settingName);
		if(setting!=null){
			mav.put(V_setting, setting);
		}
		ClusterSettingKey settingPrefix = new ClusterSettingKey(settingName, null, null, null, null);
		List<ClusterSetting> settings = clusterSettingNode.getWithPrefix(settingPrefix, true, null);
		mav.put(V_settings, settings);
		return mav;
	}
	
	@Handler Mav browseSettings(){
		Mav mav = new Mav(JSP_browseSettings);
		String nodeName = params.optional(P_name, "job.");
		mav.put(V_nodeName, nodeName);
		mav.put(V_node, settingNode.getDescendantByName(nodeName));
		mav.put(V_ancestors, settingNode.getDescendanceByName(nodeName));
		mav.put(V_children, settingNode.getDescendantByName(nodeName).getListChildren());
		ArrayList<Setting<?>> settingsList = (ArrayList<Setting<?>>)settingNode.getDescendantByName(nodeName)
				.getListSettings();
		Map<String,List<ClusterSetting>> mapListsCustom = MapTool.createHashMap();
		for(Setting<?> setting : settingsList){
			ClusterSettingKey settingKey = new ClusterSettingKey(setting.getName(), null, null, null, null);
			List<ClusterSetting> clusterSettings = clusterSettingNode.getWithPrefix(settingKey, true, null);
			mapListsCustom.put(setting.getName(), clusterSettings);
		}
		mav.put(V_listSettings, settingsList);
		mav.put(V_mapListsCustomSettings, mapListsCustom);
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
