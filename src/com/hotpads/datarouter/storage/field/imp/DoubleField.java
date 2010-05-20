package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.Field;

public class DoubleField extends Field<Double>{

	public DoubleField(String name, Double value){
		super(name, value);
	}

	public DoubleField(String prefix, String name, Double value){
		super(prefix, name, value);
	}

}
