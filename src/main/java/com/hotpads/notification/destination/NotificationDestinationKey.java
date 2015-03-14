package com.hotpads.notification.destination;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class NotificationDestinationKey extends BasePrimaryKey<NotificationDestinationKey> {

	private String token;
	private NotificationDestinationAppEnum app;
	private String deviceId;

	public static class F {
		public static final String
			token = "token",
			app = "app",
			deviceId = "deviceId";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new StringField(F.token, token, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringEnumField<>(NotificationDestinationAppEnum.class, F.app, app, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.deviceId, deviceId, MySqlColumnType.MAX_LENGTH_VARCHAR));
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

	public String getDeviceId(){
		return deviceId;
	}

}
