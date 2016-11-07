package com.hotpads.handler.types.optional;

import com.hotpads.datarouter.util.core.DrNumberTool;

public class OptionalDouble extends OptionalParameter<Double>{

	public OptionalDouble(){
	}

	public OptionalDouble(Double optDouble){
		super(optDouble);
	}

	@Override
	public Class<Double> getInternalType(){
		return Double.class;
	}

	@Override
	public OptionalParameter<Double> fromString(String stringValue){
		return new OptionalDouble(DrNumberTool.getDoubleNullSafe(stringValue, null));
	}
}