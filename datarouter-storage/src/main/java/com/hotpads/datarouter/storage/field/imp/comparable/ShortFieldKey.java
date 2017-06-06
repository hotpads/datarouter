package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;

public class ShortFieldKey extends PrimitiveFieldKey<Short>{

	public ShortFieldKey(String name){
		super(name, Short.class);
	}

	@Override
	public ShortField createValueField(final Short value){
		return new ShortField(this, value);
	}
}
