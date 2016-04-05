package com.hotpads.datarouter.storage.field.imp;

import java.util.Date;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class DateFieldKey extends PrimitiveFieldKey<Date>{

	public DateFieldKey(String name){
		super(name);
	}

	private DateFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType){
		super(name, columnName, nullable, fieldGeneratorType, null);
	}

	public DateFieldKey withColumnName(String columnName){
		return new DateFieldKey(name, columnName, nullable, fieldGeneratorType);
	}

}
