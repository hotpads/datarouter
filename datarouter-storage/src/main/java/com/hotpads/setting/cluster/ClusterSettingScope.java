package com.hotpads.setting.cluster;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.setting.ServerType;

public enum ClusterSettingScope implements StringEnum<ClusterSettingScope>{

	defaultScope(100000),
	cluster(10000),
	serverType(1000),
	instance(100),
	application(10);
	
	
	/******************* fields ***************************/

	int specificity;
	
	
	/******************** constructors *********************/
	
	private ClusterSettingScope(int specificity) {
		this.specificity = specificity;
	}
	
	
	/****************** Datarouter *******************************/

	@Override
	public String getPersistentString() {
		return toString();
	}
	
//	public static List<HTMLSelectOptionBean> getHTMLSelectOptions(){
//		return EnumTool.getHTMLSelectOptions(values());
//	}
	
	public static ClusterSettingScope fromPersistentStringStatic(String s){
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}
	
	@Override
	public ClusterSettingScope fromPersistentString(String s) {
		return fromPersistentStringStatic(s);
	}
	
	public static ClusterSettingScope fromParams(ServerType serverType, String instance, String application){
		String s;
		if(DrStringTool.notEmpty(application)){
			s = "application";
		}else if(DrStringTool.notEmpty(instance)){
			s = "instance";
		}else if(DrObjectTool.notEquals(ServerType.UNKNOWN, serverType.getPersistentString())
				&& DrObjectTool.notEquals(ServerType.ALL, serverType.getPersistentString())){
			s = "serverType";
		}else if(DrObjectTool.equals(ServerType.ALL, serverType.getPersistentString())){
			s = "cluster";
		}else{
			s = "defaultScope";
		}
		return ClusterSettingScope.fromPersistentStringStatic(s);
	}
	
	/*********************** get/set *********************************/

	public int getSpecificity() {
		return specificity;
	}
	
	
	
}
