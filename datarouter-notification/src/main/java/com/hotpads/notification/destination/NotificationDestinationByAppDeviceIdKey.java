package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.destination.NotificationDestinationKey.FieldKeys;

public class NotificationDestinationByAppDeviceIdKey extends BasePrimaryKey<NotificationDestinationByAppDeviceIdKey>{

	private String app;//TODO change to wrapper class
	private String deviceId;
	private String token;

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationDestinationByAppDeviceIdKey(){
		this(null, null, null);
	}

	public NotificationDestinationByAppDeviceIdKey(String app, String deviceId){
		this(app, deviceId, null);
	}

	public NotificationDestinationByAppDeviceIdKey(String app, String deviceId, String token){
		this.app = app;
		this.deviceId = deviceId;
		this.token = token;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.app, app),
				new StringField(FieldKeys.deviceId, deviceId),
				new StringField(FieldKeys.token, token));
	}

	public String getApp(){
		return app;
	}

	public String getDeviceId(){
		return deviceId;
	}

	public String getToken(){
		return token;
	}

}
