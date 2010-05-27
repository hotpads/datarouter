package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;

public class FloatField extends PrimitiveField<Float>{

	public FloatField(String name, Float value){
		super(name, value);
	}

	public FloatField(String prefix, String name, Float value){
		super(prefix, name, value);
	}

	@Override
	public Float parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Float)obj;
	}
}
