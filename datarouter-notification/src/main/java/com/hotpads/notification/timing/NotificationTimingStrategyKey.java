package com.hotpads.notification.timing;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationTimingStrategyKey extends BasePrimaryKey<NotificationTimingStrategyKey>{
	private String name;

	public NotificationTimingStrategyKey(){
	}

	public NotificationTimingStrategyKey(String name){
		this.name = name;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.name, name));
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public static class FieldKeys{
		public static final StringFieldKey name = new StringFieldKey("name");
	}
}