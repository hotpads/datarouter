package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.util.core.DrComparableTool;

public abstract class BasePrimitiveField<T extends Comparable<T>>
extends BaseField<T>{

	private final PrimitiveFieldKey<T> key;

	public BasePrimitiveField(PrimitiveFieldKey<T> key, T value){
		this(null, key, value);
	}

	public BasePrimitiveField(String prefix, PrimitiveFieldKey<T> key, T value){
		super(prefix, value);
		this.key = key;
	}

	@Override
	public FieldKey<T> getKey(){
		return key;
	}

	@Override
	public int compareTo(Field<T> other){
		if(other == null){
			return 1;
		}
		return DrComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	}

	@Override
	public String getValueString(){
		if(value == null){
			return "";
		}
		return value.toString();
	}

}
