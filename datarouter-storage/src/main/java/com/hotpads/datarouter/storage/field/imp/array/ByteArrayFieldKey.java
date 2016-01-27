package com.hotpads.datarouter.storage.field.imp.array;

import com.hotpads.datarouter.storage.field.BaseFieldKey;

public class ByteArrayFieldKey extends BaseFieldKey<byte[]>{
	
	private final int size;

	
	public ByteArrayFieldKey(String name, int size){
		super(name);
		this.size = size;
	}

	
	/*********************** ByteEncodedField ***********************/
	
	@Override
	public boolean isFixedLength(){
		return false;
	}
	
	
	/**************************** get/set ****************************/
	
	public int getSize(){
		return size;
	}
	
	
}