package com.hotpads.datarouter.storage.field;

import java.util.List;

import com.google.gson.Gson;
import com.hotpads.datarouter.storage.field.imp.array.KeyedListField;

public abstract class BaseListField<V extends Comparable<V>,L extends List<V>>
extends KeyedListField<V,L,ListFieldKey<V,L>>{

	private static Gson gson = new Gson();

	public BaseListField(ListFieldKey<V,L> key, L value){
		super(key, value);
	}

	public BaseListField(String name, L value){
		this(null, name, value);
	}

	public BaseListField(String prefix, String name, L value){
		super(prefix, new ListFieldKey<>(name), value);
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return new Gson().toJson(value);
	}
}
