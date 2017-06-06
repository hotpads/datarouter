package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;

public class VarIntFieldKey extends PrimitiveFieldKey<Integer>{

	public VarIntFieldKey(String name){
		super(name, Integer.class);
	}

	@Override
	public VarIntField createValueField(final Integer value){
		return new VarIntField(this, value);
	}
}
