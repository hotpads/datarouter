package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.destination.NotificationDestinationKey.FieldKeys;

public class NotificationDestinationByAppDeviceIdKey extends BasePrimaryKey<NotificationDestinationByAppDeviceIdKey>{

	private NotificationDestinationAppName app;
	private String deviceId;
	private String token;

	NotificationDestinationByAppDeviceIdKey(){
		this.app = new NotificationDestinationAppName();
	}

	public NotificationDestinationByAppDeviceIdKey(NotificationDestinationAppName app, String deviceId){
		this(app, deviceId, null);
	}

	public NotificationDestinationByAppDeviceIdKey(NotificationDestinationAppName app, String deviceId, String token){
		this.app = app == null ? new NotificationDestinationAppName() : app;
		this.deviceId = deviceId;
		this.token = token;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField("app", FieldKeys.app, app == null ? null : app.persistentString),
				new StringField(FieldKeys.deviceId, deviceId),
				new StringField(FieldKeys.token, token));
	}

	public NotificationDestinationAppName getApp(){
		return app;
	}

	public String getDeviceId(){
		return deviceId;
	}

	public String getToken(){
		return token;
	}

}
