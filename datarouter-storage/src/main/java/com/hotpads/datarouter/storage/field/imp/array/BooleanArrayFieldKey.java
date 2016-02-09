package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.ListFieldKey;

public class BooleanArrayFieldKey extends ListFieldKey<Boolean,List<Boolean>>{

	public BooleanArrayFieldKey(String name){
		super(name);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

}