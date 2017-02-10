package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.destination.NotificationDestinationApp;

public class NotificationTypeAndDestinationAppToTemplateKey
extends BasePrimaryKey<NotificationTypeAndDestinationAppToTemplateKey>{
	private String notificationType;
	private NotificationDestinationApp notificationDestinationApp;

	public NotificationTypeAndDestinationAppToTemplateKey(){
		this.notificationDestinationApp = new NotificationDestinationApp();
	}

	public NotificationTypeAndDestinationAppToTemplateKey(String notificationType,
			NotificationDestinationApp notificationDestinationApp){
		this.notificationType = notificationType;
		this.notificationDestinationApp = notificationDestinationApp == null ? new NotificationDestinationApp() :
			notificationDestinationApp;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.notificationType, notificationType),
				new StringField("notificationDestinationApp", FieldKeys.notificationDestinationApp,
						notificationDestinationApp.persistentString));
	}

	public String getNotificationType(){
		return notificationType;
	}

	public void setNotificationType(String notificationType){
		this.notificationType = notificationType;
	}

	public NotificationDestinationApp getNotificationDestinationApp(){
		return notificationDestinationApp;
	}

	public void setNotificationDestinationApp(NotificationDestinationApp notificationDestinationApp){
		this.notificationDestinationApp = notificationDestinationApp;
	}

	public static class FieldKeys{
		public static final StringFieldKey notificationType = new StringFieldKey("notificationType");
		public static final StringFieldKey notificationDestinationApp = NotificationDestinationApp.key.withColumnName(
				"notificationDestinationApp");
	}
}
