package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.Field;

public class IntegerField extends Field<Integer>{

	public IntegerField(String name, Integer value){
		super(name, value);
	}

	public IntegerField(String prefix, String name, Integer value){
		super(prefix, name, value);
	}

}
