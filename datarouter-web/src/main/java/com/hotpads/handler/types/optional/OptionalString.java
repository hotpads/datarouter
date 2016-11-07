package com.hotpads.handler.types.optional;

public class OptionalString extends OptionalParameter<String>{

	public OptionalString(){
	}

	public OptionalString(String optString){
		super(optString);
	}

	@Override
	public Class<String> getInternalType(){
		return String.class;
	}

	@Override
	public OptionalParameter<String> fromString(String stringValue){
		return new OptionalString(stringValue);
	}
}