package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class BooleanFieldKey extends PrimitiveFieldKey<Boolean>{

	public BooleanFieldKey(String name){
		super(name);
	}

	private BooleanFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Boolean defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	public BooleanFieldKey withDefaultValue(Boolean defaultValueOverride){
		return new BooleanFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValueOverride);
	}

	public BooleanFieldKey withColumnName(String columnNameOverride){
		return new BooleanFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}
}
