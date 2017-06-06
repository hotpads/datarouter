package com.hotpads.datarouter.storage.field.imp.array;

import com.hotpads.datarouter.storage.field.BaseFieldKey;

public class PrimitiveLongArrayFieldKey extends BaseFieldKey<long[]>{

	public PrimitiveLongArrayFieldKey(String name){
		super(name, long[].class);
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
	public PrimitiveLongArrayField createValueField(final long[] value){
		return new PrimitiveLongArrayField(this, value);
	}
}