package com.hotpads.datarouter.storage.field.imp.array;

import com.hotpads.datarouter.storage.field.BaseFieldKey;

public class PrimitiveIntegerArrayFieldKey extends BaseFieldKey<int[]>{
	
	public PrimitiveIntegerArrayFieldKey(String name){
		super(name);
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
	
}