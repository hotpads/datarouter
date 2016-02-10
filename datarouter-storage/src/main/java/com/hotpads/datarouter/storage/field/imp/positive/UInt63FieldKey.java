package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class UInt63FieldKey extends PrimitiveFieldKey<Long>{

	public UInt63FieldKey(String name){
		super(name);
	}

	public UInt63FieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Long defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	public UInt63FieldKey withFieldGeneratorType(FieldGeneratorType fieldGeneratorType){
		return new UInt63FieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

}
