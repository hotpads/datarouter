package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationDestinationKey extends BasePrimaryKey<NotificationDestinationKey>{

	private String token;
	private NotificationDestinationAppEnum app;
	private String deviceId;

	public static class F{
		public static final String
			token = "token",
			app = "app",
			deviceId = "deviceId";
	}

	public static class FieldKeys{
		public static final StringFieldKey token = new StringFieldKey("token");
		public static final StringEnumFieldKey<NotificationDestinationAppEnum> app = new StringEnumFieldKey<>("app",
				NotificationDestinationAppEnum.class);
		public static final StringFieldKey deviceId = new StringFieldKey("deviceId");
	}


	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.token, token),
				new StringEnumField<>(FieldKeys.app, app),
				new StringField(FieldKeys.deviceId, deviceId));
	}

	NotificationDestinationKey(){
	}

	public NotificationDestinationKey(String token, NotificationDestinationAppEnum app, String deviceId){
		this.token = token;
		this.app = app;
		this.deviceId = deviceId;
	}

	public NotificationDestinationAppEnum getApp(){
		return app;
	}

	public void setApp(NotificationDestinationAppEnum app){
		this.app = app;
	}

	public String getDeviceId(){
		return deviceId;
	}

	public String getToken(){
		return token;
	}

}
