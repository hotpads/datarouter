package com.hotpads.datarouter.storage.field.imp.custom;

import java.util.Date;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class LongDateFieldKey extends PrimitiveFieldKey<Date>{

	public LongDateFieldKey(String name){
		super(name, Date.class);
	}

	private LongDateFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Date defaultValue){
		super(name, columnName, nullable, Date.class, fieldGeneratorType, defaultValue);
	}

	public LongDateFieldKey withColumnName(String columnName){
		return new LongDateFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	@Override
	public LongDateField createValueField(final Date value){
		return new LongDateField(this, value);
	}
}
