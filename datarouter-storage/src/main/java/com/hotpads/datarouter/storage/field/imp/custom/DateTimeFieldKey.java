package com.hotpads.datarouter.storage.field.imp.custom;

import java.time.LocalDateTime;

import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class DateTimeFieldKey extends BaseFieldKey<LocalDateTime>{

	public DateTimeFieldKey(String name){
		super(name);
	}

	private DateTimeFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			LocalDateTime defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	public DateTimeFieldKey withColumnName(String columnNameOverride){
		return new DateTimeFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}
}
