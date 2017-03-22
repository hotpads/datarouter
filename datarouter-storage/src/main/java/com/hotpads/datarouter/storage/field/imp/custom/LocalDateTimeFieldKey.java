package com.hotpads.datarouter.storage.field.imp.custom;

import java.time.LocalDateTime;

import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class LocalDateTimeFieldKey extends BaseFieldKey<LocalDateTime>{

	public LocalDateTimeFieldKey(String name){
		super(name);
	}

	private LocalDateTimeFieldKey(String name, String columnName, boolean nullable,
			FieldGeneratorType fieldGeneratorType, LocalDateTime defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	public LocalDateTimeFieldKey withColumnName(String columnNameOverride){
		return new LocalDateTimeFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}
}
