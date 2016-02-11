package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.ListFieldKey;

public class UInt7ArrayFieldKey extends ListFieldKey<Byte,List<Byte>>{

	public UInt7ArrayFieldKey(String name){
		super(name);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

}