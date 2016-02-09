package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.ListFieldKey;

public class DoubleArrayFieldKey extends ListFieldKey<Double,List<Double>>{

	public DoubleArrayFieldKey(String name){
		super(name);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

}