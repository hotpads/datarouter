package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.util.core.enums.IntegerEnum;
import com.hotpads.util.core.java.ReflectionTool;

public class IntegerEnumFieldKey<E extends IntegerEnum<E>> extends BaseFieldKey<E>{

	private E sampleValue;

	public IntegerEnumFieldKey(String name, Class<E> enumClass){
		super(name);
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	private IntegerEnumFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			E defaultValue, E sampleValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
		this.sampleValue = sampleValue;
	}

	public IntegerEnumFieldKey<E> withColumnName(String columnNameOverride){
		return new IntegerEnumFieldKey<>(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue,
				sampleValue);
	}

	public E getSampleValue(){
		return sampleValue;
	}

}