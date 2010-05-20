package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;

public class IntegerField extends PrimitiveField<Integer>{

	public IntegerField(String name, Integer value){
		super(name, value);
	}

	public IntegerField(String prefix, String name, Integer value){
		super(prefix, name, value);
	}

}
