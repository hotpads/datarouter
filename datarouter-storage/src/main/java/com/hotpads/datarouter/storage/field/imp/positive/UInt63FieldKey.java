package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.util.core.number.RandomTool;

public class UInt63FieldKey extends PrimitiveFieldKey<Long>{

	public UInt63FieldKey(String name){
		super(name);
	}

	private UInt63FieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Long defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	public UInt63FieldKey withFieldGeneratorType(FieldGeneratorType fieldGeneratorTypeOverride){
		return new UInt63FieldKey(name, columnName, nullable, fieldGeneratorTypeOverride, defaultValue);
	}

	public UInt63FieldKey withColumnName(String columnNameOverride){
		return new UInt63FieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}

	public UInt63FieldKey withNullable(boolean nullableOverride){
		return new UInt63FieldKey(name, columnName, nullableOverride, fieldGeneratorType, defaultValue);
	}

	@Override
	public Long generateRandomValue(){
		return RandomTool.nextPositiveLong();
	}

}
