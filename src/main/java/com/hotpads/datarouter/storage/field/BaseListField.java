package com.hotpads.datarouter.storage.field;

import java.util.List;

import com.hotpads.util.core.CollectionTool;

public abstract class BaseListField<V extends Comparable<V>,L extends List<V>>
extends BaseField<L>{

	public BaseListField(String name, L value) {
		super(null, name, value);
	}
	
	public BaseListField(String prefix, String name, L value) {
		super(prefix,name,value);
	}
	
	@Override
	public int compareTo(Field<L> other){
		if(other==null){ return 1; }
		return this.toString().compareTo(other.toString());
	}
	
	@Override
	public boolean isCollection(){
		return true;
	}
	
	public L getValues(){
		return value;
	}
	
	public int size(){
		return CollectionTool.size(value);
	}
	
	@Override
	public String getValueString(){
		return String.valueOf(value);
	}

	@Override
	public String getSqlEscaped(){
		if(value==null){
			return "null";
		}
		return value.toString();
	}
}
