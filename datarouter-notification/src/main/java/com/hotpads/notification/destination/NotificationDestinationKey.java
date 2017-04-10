package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationDestinationKey extends BasePrimaryKey<NotificationDestinationKey>{

	private String token;
	private NotificationDestinationAppName app;
	private String deviceId;

	public static class FieldKeys{
		public static final StringFieldKey token = new StringFieldKey("token");
		public static final StringFieldKey app = NotificationDestinationAppName.key.withColumnName("app");
		public static final StringFieldKey deviceId = new StringFieldKey("deviceId");
	}


	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.token, token),
				new StringField("app", FieldKeys.app, app.persistentString),
				new StringField(FieldKeys.deviceId, deviceId));
	}

	NotificationDestinationKey(){
		this.app = new NotificationDestinationAppName();
	}

	public NotificationDestinationKey(String token, NotificationDestinationAppName app, String deviceId){
		this.token = token;
		this.app = app == null ? new NotificationDestinationAppName() : app;
		this.deviceId = deviceId;
	}

	public NotificationDestinationAppName getApp(){
		return app;
	}

	public void setApp(NotificationDestinationAppName app){
		this.app = app;
	}

	public String getDeviceId(){
		return deviceId;
	}

	public String getToken(){
		return token;
	}

}
