package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.common.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.ListFieldKey;

public class UInt63ArrayFieldKey extends ListFieldKey<Long,List<Long>>{

	public UInt63ArrayFieldKey(String name){
		super(name, new TypeToken<List<Long>>(){});
	}

	@Override
	public UInt63ArrayField createValueField(final List<Long> value){
		return new UInt63ArrayField(this, value);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

}