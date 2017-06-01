package com.hotpads.datarouter.storage.field.imp.dumb;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;

public class DumbFloatFieldKey extends PrimitiveFieldKey<Float>{

	public DumbFloatFieldKey(String name){
		super(name, Float.class);
	}

	@Override
	public DumbFloatField createValueField(final Float value){
		return new DumbFloatField(this, value);
	}
}
