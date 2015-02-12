package com.hotpads.setting;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;

@SuppressWarnings("serial")
public class GlobalSetting extends BaseDatabean<GlobalSettingKey,GlobalSetting>{

	private GlobalSettingKey key;
	private String value;

	/********************** columns ************************/

	public static class F{
		public static final String value = "value";
	}

	public static class GlobalSettingFielder extends BaseDatabeanFielder<GlobalSettingKey,GlobalSetting>{
		@Override
		public Class<GlobalSettingKey> getKeyFielderClass(){
			return GlobalSettingKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(GlobalSetting d){
			return FieldTool.createList(
					new StringField(F.value, d.value, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
		
		@Override
		public MySqlCharacterSet getCharacterSet(GlobalSetting databean){
			return MySqlCharacterSet.latin1;
		}
		@Override
		public MySqlCollation getCollation(GlobalSetting databean){
			return MySqlCollation.latin1_swedish_ci;
		}
	}

	/************************* constructors ***************************/

	@SuppressWarnings("unused")
	private GlobalSetting(){
		this.key = new GlobalSettingKey(null);
	}

	public GlobalSetting(GlobalSettingKey key, String value){
		this.key = key;
		this.value = value;
	}

	public GlobalSetting(String name, String value){
		this.key = new GlobalSettingKey(name);
		this.value = value;
	}

	/******************************* databean **************************/

	@Override
	public Class<GlobalSettingKey> getKeyClass(){
		return GlobalSettingKey.class;
	}

	@Override
	public GlobalSettingKey getKey(){
		return key;
	}

	/******************************* getters/setters *****************************/

	public String getValue(){
		return value;
	}

	public void setValue(String value){
		this.value = value;
	}

	public void setKey(GlobalSettingKey key){
		this.key = key;
	}

}