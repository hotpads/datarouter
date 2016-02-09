package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.ListFieldKey;

public class IntegerArrayFieldKey extends ListFieldKey<Integer,List<Integer>>{

	public IntegerArrayFieldKey(String name){
		super(name);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

}