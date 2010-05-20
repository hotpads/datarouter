package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.Field;

public class CharacterField extends Field<Character>{

	public CharacterField(String name, Character value){
		super(name, value);
	}

	public CharacterField(String prefix, String name, Character value){
		super(prefix, name, value);
	}

}
