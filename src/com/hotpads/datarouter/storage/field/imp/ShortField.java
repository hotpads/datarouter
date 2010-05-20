package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.Field;

public class ShortField extends Field<Short>{

	public ShortField(String name, Short value){
		super(name, value);
	}

	public ShortField(String prefix, String name, Short value){
		super(prefix, name, value);
	}

}
