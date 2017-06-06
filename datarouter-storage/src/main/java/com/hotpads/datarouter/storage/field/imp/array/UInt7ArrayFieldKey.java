package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.common.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.ListFieldKey;

public class UInt7ArrayFieldKey extends ListFieldKey<Byte,List<Byte>>{

	public UInt7ArrayFieldKey(String name){
		super(name, new TypeToken<List<Byte>>(){});
	}

	@Override
	public UInt7ArrayField createValueField(final List<Byte> value){
		return new UInt7ArrayField(this, value);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public boolean isFixedLength(){
		return false;
	}

}