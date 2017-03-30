package com.hotpads.clustersetting;

import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;

public enum ClusterSettingLogAction implements StringEnum<ClusterSettingLogAction>{
	INSERTED("inserted"),
	UPDATED("updated"),
	DELETED("deleted");

	private String persistentString;


	private ClusterSettingLogAction(String value){
		this.persistentString = value;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public ClusterSettingLogAction fromPersistentString(String string){
		return fromPersistentStringStatic(string);
	}

	public static ClusterSettingLogAction fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

}
