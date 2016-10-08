package com.hotpads.notification.destination;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.destination.NotificationDestinationKey.F;

public class NotificationDestinationByAppDeviceIdKey extends BasePrimaryKey<NotificationDestinationByAppDeviceIdKey>{

	private NotificationDestinationAppEnum app;
	private String deviceId;
	private String token;

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationDestinationByAppDeviceIdKey(){
		this(null, null, null);
	}

	public NotificationDestinationByAppDeviceIdKey(NotificationDestinationAppEnum app, String deviceId){
		this(app, deviceId, null);
	}

	public NotificationDestinationByAppDeviceIdKey(NotificationDestinationAppEnum app, String deviceId, String token){
		this.app = app;
		this.deviceId = deviceId;
		this.token = token;
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringEnumField<>(NotificationDestinationAppEnum.class, F.app, app, MySqlColumnType
						.MAX_LENGTH_VARCHAR),
				new StringField(F.deviceId, deviceId, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.token, token, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	public NotificationDestinationAppEnum getApp(){
		return app;
	}

	public String getDeviceId(){
		return deviceId;
	}

	public String getToken(){
		return token;
	}

}
