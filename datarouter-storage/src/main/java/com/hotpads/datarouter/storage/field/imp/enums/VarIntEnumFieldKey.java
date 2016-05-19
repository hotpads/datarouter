package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.util.core.java.ReflectionTool;

public class VarIntEnumFieldKey<E extends IntegerEnum<E>> extends BaseFieldKey<E>{

	private final Class<E> enumClass;
	private final E sampleValue;

	public VarIntEnumFieldKey(String name, Class<E> enumClass){
		super(name);
		this.enumClass = enumClass;
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	public Class<E> getEnumClass(){
		return enumClass;
	}

	public E getSampleValue(){
		return sampleValue;
	}

}