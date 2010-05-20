package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.Field;

public class FloatField extends Field<Float>{

	public FloatField(String name, Float value){
		super(name, value);
	}

	public FloatField(String prefix, String name, Float value){
		super(prefix, name, value);
	}

}
