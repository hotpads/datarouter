package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.notification.destination.NotificationDestinationApp;

public class NotificationTypeAndDestinationAppToTemplate
extends BaseDatabean<NotificationTypeAndDestinationAppToTemplateKey,NotificationTypeAndDestinationAppToTemplate>{
	private NotificationTypeAndDestinationAppToTemplateKey key;
	private String notificationTemplate;

	public NotificationTypeAndDestinationAppToTemplate(){
		this.key = new NotificationTypeAndDestinationAppToTemplateKey();
	}

	public NotificationTypeAndDestinationAppToTemplate(String notificationType,
			NotificationDestinationApp notificationDestinationApp, String notificationTemplate){
		this.key = new NotificationTypeAndDestinationAppToTemplateKey(notificationType, notificationDestinationApp);
		this.notificationTemplate = notificationTemplate;
	}

	public NotificationTypeAndDestinationAppToTemplate(String notificationType, String notificationDestinationApp,
			String notificationTemplate){
		this.key = new NotificationTypeAndDestinationAppToTemplateKey(notificationType,
				new NotificationDestinationApp(notificationDestinationApp));
		this.notificationTemplate = notificationTemplate;
	}

	@Override
	public Class<NotificationTypeAndDestinationAppToTemplateKey> getKeyClass(){
		return NotificationTypeAndDestinationAppToTemplateKey.class;
	}

	@Override
	public NotificationTypeAndDestinationAppToTemplateKey getKey(){
		return key;
	}

	public String getNotificationTemplate(){
		return notificationTemplate;
	}

	public void setNotificationTemplate(String notificationTemplate){
		this.notificationTemplate = notificationTemplate;
	}

	public static class FieldKeys{
		public static final StringFieldKey notificationTemplate = new StringFieldKey("notificationTemplate");
	}

	public static class NotificationTypeAndDestinationAppToTemplateFielder
	extends BaseDatabeanFielder
	<NotificationTypeAndDestinationAppToTemplateKey,NotificationTypeAndDestinationAppToTemplate>{
		public NotificationTypeAndDestinationAppToTemplateFielder(){
			super(NotificationTypeAndDestinationAppToTemplateKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTypeAndDestinationAppToTemplate databean){
			return Arrays.asList(new StringField(FieldKeys.notificationTemplate, databean.notificationTemplate));
		}
	}
}
