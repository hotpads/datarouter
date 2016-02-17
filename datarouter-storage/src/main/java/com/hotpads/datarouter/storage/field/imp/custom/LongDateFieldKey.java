package com.hotpads.datarouter.storage.field.imp.custom;

import java.util.Date;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class LongDateFieldKey extends PrimitiveFieldKey<Date>{

	public LongDateFieldKey(String name){
		super(name);
	}

	private LongDateFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Date defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	public LongDateFieldKey withColumnName(String columnName){
		return new LongDateFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

}
