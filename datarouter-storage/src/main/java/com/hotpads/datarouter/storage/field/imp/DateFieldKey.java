package com.hotpads.datarouter.storage.field.imp;

import java.util.Date;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class DateFieldKey extends PrimitiveFieldKey<Date>{

	public DateFieldKey(String name){
		super(name);
	}

	public DateFieldKey(String name, String columnName, boolean nullable){
		super(name, columnName, nullable, FieldGeneratorType.NONE);
	}
}
