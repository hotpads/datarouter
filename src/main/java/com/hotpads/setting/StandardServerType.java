package com.hotpads.setting;

import java.util.List;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.util.core.web.HTMLSelectOptionBean;

public enum StandardServerType implements ServerType, StringEnum<StandardServerType>{
	
	UNKNOWN(ServerType.UNKNOWN),
	ALL(ServerType.ALL),
	WEB("web"),
	JOBLET("joblet");
	
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
		return DataRouterEnumTool.getEnumFromString(values(), required, null);
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

}
