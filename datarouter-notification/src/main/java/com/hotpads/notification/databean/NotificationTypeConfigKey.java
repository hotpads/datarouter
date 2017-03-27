package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationTypeConfigKey extends BasePrimaryKey<NotificationTypeConfigKey>{
	private String type;

	public NotificationTypeConfigKey(){
	}

	public NotificationTypeConfigKey(String type){
		this.type = type;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.type, type));
	}

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}

	public static class FieldKeys{
		public static final StringFieldKey type = new StringFieldKey("type");
	}
}
