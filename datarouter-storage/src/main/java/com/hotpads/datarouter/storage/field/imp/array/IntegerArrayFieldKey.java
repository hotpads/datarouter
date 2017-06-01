package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.common.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.ListFieldKey;

public class IntegerArrayFieldKey extends ListFieldKey<Integer,List<Integer>>{

	public IntegerArrayFieldKey(String name){
		super(name, new TypeToken<List<Integer>>(){});
	}

	@Override
	public IntegerArrayField createValueField(final List<Integer> value){
		return new IntegerArrayField(this, value);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

}