package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class StringField extends Field<String>{
	

	public StringField(String name, String value){
		super(name, value);
	}

	public StringField(String prefix, String name, String value){
		super(prefix, name, value);
	}
	
	@Override
	public int compareTo(Field<String> other){
		if(other==null){ return -1; }
		return ComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	};

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
	
	@Override
	public byte[] getBytes(){
		return StringByteTool.getByteArray(this.value, StringByteTool.CHARSET_UTF8);
	}
	
}
