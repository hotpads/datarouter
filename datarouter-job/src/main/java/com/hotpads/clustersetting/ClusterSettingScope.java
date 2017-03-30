package com.hotpads.clustersetting;

import java.util.Objects;

import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;

public enum ClusterSettingScope implements StringEnum<ClusterSettingScope>{

	defaultScope(100000),
	cluster(10000),
	serverType(1000),
	serverName(100),
	application(10);


	/******************* fields ***************************/

	int specificity;


	/******************** constructors *********************/

	private ClusterSettingScope(int specificity){
		this.specificity = specificity;
	}


	/****************** Datarouter *******************************/

	@Override
	public String getPersistentString(){
		return toString();
	}

	public static ClusterSettingScope fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

	@Override
	public ClusterSettingScope fromPersistentString(String str){
		return fromPersistentStringStatic(str);
	}

	public static ClusterSettingScope fromParams(ServerType serverType, String serverName, String application){
		String str;
		if(DrStringTool.notEmpty(application)){
			str = "application";
		}else if(DrStringTool.notEmpty(serverName)){
			str = "serverName";
		}else if(DrObjectTool.notEquals(ServerType.UNKNOWN, serverType.getPersistentString())
				&& DrObjectTool.notEquals(ServerType.ALL, serverType.getPersistentString())){
			str = "serverType";
		}else if(Objects.equals(ServerType.ALL, serverType.getPersistentString())){
			str = "cluster";
		}else{
			str = "defaultScope";
		}
		return ClusterSettingScope.fromPersistentStringStatic(str);
	}

	/*********************** get/set *********************************/

	public int getSpecificity(){
		return specificity;
	}

}
