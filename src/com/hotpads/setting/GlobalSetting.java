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

	protected GlobalSettingKey key;
	protected String value;
	public static final String VALUE = "value";

	public GlobalSetting(){
		this.key = new GlobalSettingKey(null);
	}

	public GlobalSetting(GlobalSettingKey key, String value){
		this.key = key;
		this.value = value;
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
		return FieldTool.createList(new StringField(VALUE, value, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

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

	@Override
	public String toString(){
		return key.toString() + ":" + value;
	}

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