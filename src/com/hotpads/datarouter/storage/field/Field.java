package com.hotpads.datarouter.storage.field;

import com.hotpads.util.core.StringTool;

public class Field {

	protected String prefix;
	protected String name;
	protected Comparable<?> value;

	
	public Field(String name, Comparable<?> value) {
		this(null, name, value);
	}
	
	public Field(String prefix, String name, Comparable<?> value) {
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

	public Comparable<?> getValue() {
		return value;
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
	
	
}
