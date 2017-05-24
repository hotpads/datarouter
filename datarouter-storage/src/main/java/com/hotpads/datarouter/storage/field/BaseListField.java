package com.hotpads.datarouter.storage.field;

import java.util.List;
import java.util.Optional;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.hotpads.datarouter.storage.field.imp.array.KeyedListField;

public abstract class BaseListField<V extends Comparable<V>,L extends List<V>>
extends KeyedListField<V,L,ListFieldKey<V,L>>{

	protected static final Gson gson = new Gson();

	public BaseListField(ListFieldKey<V,L> key, L value){
		super(key, value);
	}

	public BaseListField(String name, L value, TypeToken<L> valueType){
		this(null, name, value, valueType);
	}

	public BaseListField(String prefix, String name, L value, TypeToken<L> valueType){
		super(prefix, new ListFieldKey<>(name, valueType), value);
	}

	@Override
	public String getStringEncodedValue(){
		return Optional.ofNullable(value).map(gson::toJson).orElse(null);
	}
}
