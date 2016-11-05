package com.hotpads.handler.types.optional;

import com.hotpads.datarouter.util.core.DrNumberTool;

public class OptionalLong extends OptionalParameter<Long>{

	public OptionalLong(){
	}

	public OptionalLong(Long optLong){
		super(optLong);
	}

	@Override
	public Class<?> getInternalType(){
		return Long.class;
	}

	@Override
	public OptionalParameter<Long> fromString(String stringValue){
		return new OptionalLong(DrNumberTool.getLongNullSafe(stringValue, null));
	}
}