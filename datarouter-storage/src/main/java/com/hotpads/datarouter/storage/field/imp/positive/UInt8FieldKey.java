package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;

public class UInt8FieldKey extends PrimitiveFieldKey<Byte>{

	public UInt8FieldKey(String name){
		super(name, Byte.class);
	}

	@Override
	public UInt8Field createValueField(final Byte value){
		return new UInt8Field(this, value);
	}
}
