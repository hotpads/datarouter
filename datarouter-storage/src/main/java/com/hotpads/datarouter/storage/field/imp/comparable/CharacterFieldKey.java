package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;

public class CharacterFieldKey extends PrimitiveFieldKey<Character>{

	public CharacterFieldKey(String name){
		super(name, Character.class);
	}

	@Override
	public CharacterField createValueField(final Character value){
		return new CharacterField(this, value);
	}
}
