package com.hotpads.datarouter.storage.field;

import java.util.List;

import com.google.common.reflect.TypeToken;

public class ListFieldKey<V extends Comparable<V>,L extends List<V>>
extends BaseFieldKey<L>{

	public ListFieldKey(String name, TypeToken<L> valueType){
		super(name, valueType);
	}

	@Override
	public boolean isCollection(){
		return true;
	}

}