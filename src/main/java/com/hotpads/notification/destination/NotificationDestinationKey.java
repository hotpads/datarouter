package com.hotpads.notification.destination;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.databean.NotificationUserType;

@SuppressWarnings("serial")
public class NotificationDestinationKey extends BasePrimaryKey<NotificationDestinationKey> {

	/** fields ****************************************************************/

	private NotificationUserType notificationUserType;
	private String app;
	private String token;
	private String deviceId;

	/** columns ***************************************************************/

	public static class F {
		public static final String
			notificationUserType = "notificationUserType",
			app = "app",
			deviceId = "deviceId",
			token = "token";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new StringEnumField<>(NotificationUserType.class, F.notificationUserType, notificationUserType, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.app, app, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.token, token, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.deviceId, deviceId, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	/** construct *************************************************************/

	NotificationDestinationKey(){
	}

}
