package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.util.core.java.ReflectionTool;

public class IntegerEnumFieldKey<E extends IntegerEnum<E>> extends BaseFieldKey<E>{

	private E sampleValue;

	public IntegerEnumFieldKey(String name, Class<E> enumClass){
		super(name);
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	public E getSampleValue(){
		return sampleValue;
	}

}