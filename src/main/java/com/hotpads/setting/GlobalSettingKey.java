package com.hotpads.setting;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class GlobalSettingKey extends BasePrimaryKey<GlobalSettingKey>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	/********************** fields ****************************************/

	private String name;

	public static class F{
		public static final String name = "name";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(new StringField(F.name, name, DEFAULT_STRING_LENGTH));
	}

	/*************************** constructors *******************************/

	GlobalSettingKey(){// required no-arg
	}

	public GlobalSettingKey(String name){
		this.name = name;
	}

	/****************************** methods ********************************/

	/***************************** get/set *******************************/

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}
}
