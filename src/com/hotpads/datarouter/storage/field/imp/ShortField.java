package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;

public class ShortField extends PrimitiveField<Short>{

	public ShortField(String name, Short value){
		super(name, value);
	}

	public ShortField(String prefix, String name, Short value){
		super(prefix, name, value);
	}

	@Override
	public Short parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Short)obj;
	}

}
