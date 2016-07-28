package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class BooleanFieldKey extends PrimitiveFieldKey<Boolean>{

	public BooleanFieldKey(String name){
		super(name);
	}

	public BooleanFieldKey(String name, Boolean defaultValue){
		super(name, defaultValue);
	}

	public BooleanFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType){
		super(name, columnName, nullable, fieldGeneratorType);
	}

	public BooleanFieldKey withDefaultValue(Boolean defaultValueOverride){
		return new BooleanFieldKey(name, defaultValueOverride);
	}
}
