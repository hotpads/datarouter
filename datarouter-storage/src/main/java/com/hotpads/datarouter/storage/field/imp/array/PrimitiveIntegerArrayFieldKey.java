package com.hotpads.datarouter.storage.field.imp.array;

import com.hotpads.datarouter.storage.field.BaseFieldKey;

public class PrimitiveIntegerArrayFieldKey extends BaseFieldKey<int[]>{

	public PrimitiveIntegerArrayFieldKey(String name){
		super(name, int[].class);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

	@Override
	public boolean isCollection(){
		return true;
	}

	@Override
	public PrimitiveIntegerArrayField createValueField(final int[] value){
		return new PrimitiveIntegerArrayField(this, value);
	}
}