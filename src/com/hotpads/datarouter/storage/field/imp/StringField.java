package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.Field;

public class StringField extends Field<String>{

	public StringField(String name, String value){
		super(name, value);
	}

	public StringField(String prefix, String name, String value){
		super(prefix, name, value);
	}

	public String getSqlEscaped(){
		if(value==null){
			return "null";
		}
		String stringValue = (String)value;
		return "'" + stringValue.replaceAll("'", "''") + "'";
	}

	@Override
	public String parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(String)obj;
	}
}
