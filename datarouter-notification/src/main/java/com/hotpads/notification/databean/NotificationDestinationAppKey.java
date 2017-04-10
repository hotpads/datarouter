package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.destination.NotificationDestinationAppName;

public class NotificationDestinationAppKey extends BasePrimaryKey<NotificationDestinationAppKey>{
	private NotificationDestinationAppName name;

	public NotificationDestinationAppKey(){
		name = new NotificationDestinationAppName();
	}

	public NotificationDestinationAppKey(NotificationDestinationAppName name){
		this.name = name == null ? new NotificationDestinationAppName() : name;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField("name", FieldKeys.name, name.persistentString));
	}

	public NotificationDestinationAppName getName(){
		return name;
	}

	public void setName(NotificationDestinationAppName name){
		this.name = name;
	}

	public static class FieldKeys{
		public static final StringFieldKey name = NotificationDestinationAppName.key.withColumnName("name");
	}
}
