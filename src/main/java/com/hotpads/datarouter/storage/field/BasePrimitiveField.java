package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.util.core.DrComparableTool;

public abstract class BasePrimitiveField<T extends Comparable<T>> 
extends BaseField<T>{

	public BasePrimitiveField(String name, T value) {
		super(null, name, value);
	}
	
	public BasePrimitiveField(String prefix, String name, T value) {
		super(prefix,name,value);
	}
	
	@Override
	public int compareTo(Field<T> other){
		if(other==null){ return 1; }
		return DrComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	}
	
	@Override
	public String getValueString(){
		if(value==null){ return ""; }
		return value.toString();
	}

	@Override
	public String getSqlEscaped(){
		if(value==null){
			return "null";
		}
		return value.toString();
	}
}
