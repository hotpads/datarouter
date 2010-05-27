package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;

public class ByteField extends PrimitiveField<Byte>{

	public ByteField(String name, Byte value){
		super(name, value);
	}

	public ByteField(String prefix, String name, Byte value){
		super(prefix, name, value);
	}

	@Override
	public Byte parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Byte)obj;
	}

}
