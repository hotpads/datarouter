package com.hotpads.example;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class CakeKey extends BasePrimaryKey<CakeKey>{

	private String name;

	public static class F{
		public static final StringFieldKey name = new StringFieldKey("name");
	}

	public CakeKey(){}

	public CakeKey(String name){
		this.name = name;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(F.name, name));
	}

	public String getName(){
		return name;
	}

}
