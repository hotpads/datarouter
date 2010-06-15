package com.hotpads.datarouter.storage.field;

import com.hotpads.util.core.ComparableTool;

public abstract class PrimitiveField<T extends Comparable<T>> extends BaseField<T>{

	public PrimitiveField(String name, T value) {
		super(null, name, value);
	}
	
	public PrimitiveField(String prefix, String name, T value) {
		super(prefix,name,value);
	}
	
	@Override
	public int compareTo(BaseField<T> other){
		if(other==null){ return 1; }
		return ComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	};

	@Override
	public String getSqlEscaped(){
		if(value==null){
			return "null";
		}
		return value.toString();
	}
}
