package com.hotpads.setting;

import java.util.List;

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
			return d.getNonKeyFields();
		}
	}

	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(new StringField(F.value, value, MySqlColumnType.MAX_LENGTH_VARCHAR));
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

	@Override
	public boolean isFieldAware(){
		return true;
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