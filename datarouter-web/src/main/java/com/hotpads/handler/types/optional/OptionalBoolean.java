package com.hotpads.handler.types.optional;

import com.hotpads.datarouter.util.core.DrBooleanTool;

public class OptionalBoolean extends OptionalParameter<Boolean>{

	public OptionalBoolean(){
	}

	public OptionalBoolean(Boolean optBoolean){
		super(optBoolean);
	}

	@Override
	public Class<?> getInternalType(){
		return Boolean.class;
	}

	@Override
	public OptionalParameter<Boolean> fromString(String stringValue){
		return new OptionalBoolean(DrBooleanTool.isBoolean(stringValue) ? DrBooleanTool.isTrue(stringValue) : null);
	}
}