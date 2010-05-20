package com.hotpads.datarouter.storage.field;

public abstract class PrimitiveField<T extends Comparable<T>> extends Field<T> {

	public PrimitiveField(String name, T value) {
		super(null, name, value);
	}
	
	public PrimitiveField(String prefix, String name, T value) {
		super(prefix,name,value);
	}

	@Override
	public String getSqlEscaped(){
		if(value==null){
			return "null";
		}
		return value.toString();
	}
}
