package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class PrimitiveFieldKey<T extends Comparable<T>>
extends BaseFieldKey<T>{

	public PrimitiveFieldKey(String name){
		super(name);
	}

	public PrimitiveFieldKey(String name, T defaultValue){
		super(name, defaultValue);
	}

	public PrimitiveFieldKey(String name, boolean nullable, FieldGeneratorType fieldGeneratorType){
		super(name, nullable, fieldGeneratorType);
	}

	public PrimitiveFieldKey(String name, String columnName, boolean nullable,
			FieldGeneratorType fieldGeneratorType){
		super(name, columnName, nullable, fieldGeneratorType);
	}

	public PrimitiveFieldKey(String name, FieldGeneratorType fieldGeneratorType){
		super(name, fieldGeneratorType);
	}

}