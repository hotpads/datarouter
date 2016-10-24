package com.hotpads.datarouter.setting;

import java.util.List;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.util.core.web.HTMLSelectOptionBean;

public enum StandardServerType implements ServerType, StringEnum<StandardServerType>{

	UNKNOWN(ServerType.UNKNOWN),
	ALL(ServerType.ALL),
	WEB("web"),
	JOBLET("joblet"),
	DEV(ServerType.DEV);

	private String persistentString;

	private StandardServerType(String persistentString){
		this.persistentString = persistentString;
	}

	@Override
	public List<HTMLSelectOptionBean> getHTMLSelectOptionsVarNames(){
		return null;
	}

	@Override
	public StandardServerType fromPersistentString(String required){
		return DatarouterEnumTool.getEnumFromString(values(), required, null);
	}

	public static StandardServerType fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

}
