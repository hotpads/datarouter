package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;

public class SignedByteFieldKey extends PrimitiveFieldKey<Byte>{

	public SignedByteFieldKey(String name){
		super(name, Byte.class);
	}

	@Override
	public SignedByteField createValueField(final Byte value){
		return new SignedByteField(this, value);
	}
}
