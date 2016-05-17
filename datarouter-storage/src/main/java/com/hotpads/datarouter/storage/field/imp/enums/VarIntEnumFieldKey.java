package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;

public class VarIntEnumFieldKey<E extends IntegerEnum<E>> extends BaseFieldKey<E>{

	private final Class<E> enumClass;

	public VarIntEnumFieldKey(String name, Class<E> enumClass){
		super(name);
		this.enumClass = enumClass;
	}

	public Class<E> getEnumClass(){
		return enumClass;
	}

}