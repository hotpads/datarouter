package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public abstract class PrimitiveFieldKey<T extends Comparable<T>> extends BaseFieldKey<T>{

	public PrimitiveFieldKey(String name, Class<T> valueType){
		super(name, valueType);
	}

	public PrimitiveFieldKey(String name, Class<T> valueType, T defaultValue){
		super(name, valueType, defaultValue);
	}

	public PrimitiveFieldKey(String name, boolean nullable, Class<T> valueType, FieldGeneratorType fieldGeneratorType){
		super(name, nullable, valueType, fieldGeneratorType);
	}

	public PrimitiveFieldKey(String name, String columnName, boolean nullable, Class<T> valueType,
			FieldGeneratorType fieldGeneratorType){
		super(name, columnName, nullable, valueType, fieldGeneratorType);
	}

	protected PrimitiveFieldKey(String name, String columnName, boolean nullable, Class<T> valueType,
			FieldGeneratorType fieldGeneratorType, T defaultValue){
		super(name, columnName, nullable, valueType, fieldGeneratorType, defaultValue);
	}

}