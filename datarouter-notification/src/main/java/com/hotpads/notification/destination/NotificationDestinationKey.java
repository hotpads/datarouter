package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationDestinationKey extends BasePrimaryKey<NotificationDestinationKey>{

	private String token;
	private String app;
	private String deviceId;

	public static class FieldKeys{
		public static final StringFieldKey token = new StringFieldKey("token");
		public static final StringFieldKey app = new StringFieldKey("app");
		public static final StringFieldKey deviceId = new StringFieldKey("deviceId");
	}


	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.token, token),
				new StringField(FieldKeys.app, app),
				new StringField(FieldKeys.deviceId, deviceId));
	}

	NotificationDestinationKey(){
	}

	public NotificationDestinationKey(String token, String app, String deviceId){
		this.token = token;
		this.app = app;
		this.deviceId = deviceId;
	}

	public String getApp(){
		return app;
	}

	public void setApp(String app){
		this.app = app;
	}

	public String getDeviceId(){
		return deviceId;
	}

	public String getToken(){
		return token;
	}

}
