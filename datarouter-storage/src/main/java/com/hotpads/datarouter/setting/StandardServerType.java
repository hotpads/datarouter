package com.hotpads.datarouter.setting;

import java.util.List;

import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;
import com.hotpads.util.core.web.HtmlSelectOptionBean;

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
	public List<HtmlSelectOptionBean> getHtmlSelectOptionsVarNames(){
		return null;
	}

	@Override
	public StandardServerType fromPersistentString(String str){
		return fromPersistentStringStatic(str);
	}

	public static StandardServerType fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

}
