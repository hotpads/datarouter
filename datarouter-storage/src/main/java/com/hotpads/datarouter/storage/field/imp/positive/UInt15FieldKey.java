package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;

public class UInt15FieldKey extends PrimitiveFieldKey<Short>{

	public UInt15FieldKey(String name){
		super(name, Short.class);
	}

	@Override
	public UInt15Field createValueField(final Short value){
		return new UInt15Field(this, value);
	}
}
