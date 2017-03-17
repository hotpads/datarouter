package com.hotpads.example.config;

import java.util.List;

import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.util.core.enums.EnumTool;
import com.hotpads.util.core.enums.StringEnum;
import com.hotpads.util.core.enums.StringPersistedEnum;
import com.hotpads.util.core.web.HtmlSelectOptionBean;

public enum ExampleServerType implements ServerType, StringPersistedEnum, StringEnum<ExampleServerType>{
	ALL("all"),//for factory-like usage
	DEV("dev"),
	EXAMPLE("example");

	private String persistentString;

	private ExampleServerType(String persistentString){
		this.persistentString = persistentString;
	}

	@Override
	public List<HtmlSelectOptionBean> getHtmlSelectOptionsVarNames(){
		return EnumTool.getHtmlSelectOptions(values());
	}

	@Override
	public ExampleServerType fromPersistentString(String str){
		return fromPersistentStringStatic(str);
	}

	public static ExampleServerType fromPersistentStringStatic(String str){
		return EnumTool.fromPersistentString(values(), str);
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public String getDisplay(){
		return persistentString;
	}

}