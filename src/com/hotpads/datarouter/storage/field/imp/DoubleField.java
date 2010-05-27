package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;

public class DoubleField extends PrimitiveField<Double>{

	public DoubleField(String name, Double value){
		super(name, value);
	}

	public DoubleField(String prefix, String name, Double value){
		super(prefix, name, value);
	}

	@Override
	public Double parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Double)obj;
	}

}
