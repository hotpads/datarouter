package com.hotpads.datarouter.storage.field.encoding;

public enum FieldGeneratorType{
	
	NONE(false),
	MANAGED(true),
	RANDOM(true)
	;
	
	
	private final boolean generated;

	private FieldGeneratorType(boolean generated){
		this.generated = generated;
	}
	
	
	public boolean isGenerated(){
		return generated;
	}
}