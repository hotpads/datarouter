package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.common.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.ListFieldKey;

public class BooleanArrayFieldKey extends ListFieldKey<Boolean,List<Boolean>>{

	public BooleanArrayFieldKey(String name){
		super(name, new TypeToken<List<Boolean>>(){});
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

	@Override
	public BooleanArrayField createValueField(final List<Boolean> value){
		return new BooleanArrayField(this, value);
	}
}