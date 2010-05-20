package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.Field;

public class BooleanField extends Field<Boolean>{

	public BooleanField(String name, Boolean value){
		super(name, value);
	}

	public BooleanField(String prefix, String name, Boolean value){
		super(prefix, name, value);
	}

}
