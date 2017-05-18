package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class LongFieldKey extends PrimitiveFieldKey<Long>{

	public LongFieldKey(String name){
		super(name, Long.class);
	}

	private LongFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Long defaultValue){
		super(name, columnName, nullable, Long.class, fieldGeneratorType, defaultValue);
	}

	public LongFieldKey withColumnName(String columnName){
		return new LongFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	public LongFieldKey withNullable(boolean nullableOverride){
		return new LongFieldKey(name, columnName, nullableOverride, fieldGeneratorType, defaultValue);
	}

}
