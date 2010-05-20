package com.hotpads.datarouter.storage.field;

import java.util.List;

import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

public abstract class Field<T extends Comparable<T>>{

	protected String prefix;
	protected String name;
	protected T value;

	
	public Field(String name, T value) {
		this(null, name, value);
	}
	
	public Field(String prefix, String name, T value) {
		this.prefix = prefix;
		this.name = name;
		this.value = value;
	}
	
	public String getPrefixedName(){
		if(StringTool.isEmpty(prefix)){
			return name;
		}else{
			return prefix + "." + name;
		}
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return this.getPrefixedName()+":"+this.getValue();
	}
	
	public String getSqlEscaped(){
		if(value==null){
			return "null";
		}
		if(value instanceof String){
			String stringValue = (String)value;
			return "'" + stringValue.replaceAll("'", "''") + "'";
		}
		return value.toString();
	}
	
	public String getSqlNameValuePairEscaped(){
		if(value==null){
			return this.name+" is null";
		}
		return this.name+"="+this.getSqlEscaped();
	}
	
	public static List<Field<?>> createList(Field<?>... fields){
		return ListTool.createArrayList(fields);
	}
	
	public static int countNonNullLeadingFields(Iterable<Field<?>> fields){
		int count = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(field.getValue() != null){
				++count;
			}else{
				break;
			}
		}
		return count;
	}
	
}
