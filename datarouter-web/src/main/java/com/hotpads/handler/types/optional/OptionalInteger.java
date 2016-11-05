package com.hotpads.handler.types.optional;

import com.hotpads.datarouter.util.core.DrNumberTool;

public class OptionalInteger extends OptionalParameter<Integer>{

	public OptionalInteger(){
	}

	public OptionalInteger(Integer optInt){
		super(optInt);
	}

	@Override
	public Class<?> getInternalType(){
		return Integer.class;
	}

	@Override
	public OptionalParameter<Integer> fromString(String stringValue){
		return new OptionalInteger(DrNumberTool.parseIntegerFromNumberString(stringValue, null));
	}
}