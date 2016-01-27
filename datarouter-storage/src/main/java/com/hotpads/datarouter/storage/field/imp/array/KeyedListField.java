package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.ListFieldKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public abstract class KeyedListField<V extends Comparable<V>,L extends List<V>,K extends ListFieldKey<V,L>>
extends BaseField<L>{

	protected final K key;

	public KeyedListField(K key, L value){
		super(null,value);
		this.key = key;
	}

	public KeyedListField(String prefix, K key, L value){
		super(prefix, value);
		this.key = key;
	}

	@Override
	public K getKey(){
		return key;
	}

	@Override
	public int compareTo(Field<L> other){
		if(other == null){
			return 1;
		}
		return this.toString().compareTo(other.toString());
	}

	public L getValues(){
		return value;
	}

	public int size(){
		return DrCollectionTool.size(value);
	}

	@Override
	public String getValueString(){
		return String.valueOf(value);
	}

}
