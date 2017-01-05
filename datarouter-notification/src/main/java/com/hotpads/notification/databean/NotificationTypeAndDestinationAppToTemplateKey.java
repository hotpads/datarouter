package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationTypeAndDestinationAppToTemplateKey
extends BasePrimaryKey<NotificationTypeAndDestinationAppToTemplateKey>{
	private String notificationType;
	private String notificationDestinationApp;

	public NotificationTypeAndDestinationAppToTemplateKey(){
	}

	public NotificationTypeAndDestinationAppToTemplateKey(String notificationType, String notificationDestinationApp){
		this.notificationType = notificationType;
		this.notificationDestinationApp = notificationDestinationApp;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.notificationType, notificationType),
				new StringField(FieldKeys.notificationDestinationApp, notificationDestinationApp));
	}

	public String getNotificationType(){
		return notificationType;
	}

	public void setNotificationType(String notificationType){
		this.notificationType = notificationType;
	}

	public String getNotificationDestinationApp(){
		return notificationDestinationApp;
	}

	public void setNotificationDestinationApp(String notificationDestinationApp){
		this.notificationDestinationApp = notificationDestinationApp;
	}

	public static class FieldKeys{
		public static final StringFieldKey notificationType = new StringFieldKey("notificationType");
		public static final StringFieldKey notificationDestinationApp = new StringFieldKey(
				"notificationDestinationApp");
	}
}
