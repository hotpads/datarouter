package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.StringByteTool;

public class CharacterField extends PrimitiveField<Character>{

	public CharacterField(String name, Character value){
		super(name, value);
	}

	public CharacterField(String prefix, String name, Character value){
		super(prefix, name, value);
	}

	@Override
	public Character parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Character)obj;
	}
	
	@Override
	public byte[] getBytes(){
		return StringByteTool.getByteArray(this.value.toString(), StringByteTool.CHARSET_UTF8);
	}

}
