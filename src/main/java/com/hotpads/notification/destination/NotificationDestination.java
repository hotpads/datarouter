package com.hotpads.notification.destination;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;


/** CREATE SCRIPT
com.hotpads.notification.destination.NotificationDestination{
  PK{
    StringEnumField<NotificationUserType> notificationUserType,
    StringField app,
    StringField token
    StringField deviceId,
  }
  StringEnumField<NotificationDestinationPlatform> platform,
  StringField deviceName,
  BooleanField active

}

*/
public class NotificationDestination extends BaseDatabean<NotificationDestinationKey,NotificationDestination> {

	private NotificationDestinationKey key;

	private NotificationDestinationPlatform platform;
	private String deviceName;
	private Boolean active;

	public static class F {
		public static final String
			platform = "platform",
			deviceName = "deviceName",
			active = "active";
	}

	public static class NotificationDestinationFielder
		extends BaseDatabeanFielder<NotificationDestinationKey, NotificationDestination>{

		@Override
		public Class<NotificationDestinationKey> getKeyFielderClass() {
			return NotificationDestinationKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationDestination d){
			return FieldTool.createList(
				new StringEnumField<>(NotificationDestinationPlatform.class, F.platform, d.platform, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.deviceName, d.deviceName, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new BooleanField(F.active, d.active));
		}

	}

	private NotificationDestination(){
		this.key = new NotificationDestinationKey();
	}

	/** databean **************************************************************/

	@Override
	public Class<NotificationDestinationKey> getKeyClass() {
		return NotificationDestinationKey.class;
	}

	@Override
	public NotificationDestinationKey getKey() {
		return key;
	}

}

