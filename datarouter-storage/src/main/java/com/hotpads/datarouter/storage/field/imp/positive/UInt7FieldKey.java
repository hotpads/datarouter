package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;

public class UInt7FieldKey extends PrimitiveFieldKey<Byte>{

	public UInt7FieldKey(String name){
		super(name, Byte.class);
	}

	@Override
	public UInt7Field createValueField(final Byte value){
		return new UInt7Field(this, value);
	}
}
