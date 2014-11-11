package com.hotpads.notification.tracking;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class NotificationTrackingEventKey extends BasePrimaryKey<NotificationTrackingEventKey> {

	/** fields ****************************************************************/

	private NotificationTrackingEventType eventType;
	private Date created;
	private String notificationId;

	/** columns ***************************************************************/

	public static class F {
		public static final String
			eventType = "eventType",
			created = "created",
			notificationId = "notificationId";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new StringField(F.eventType, NotificationTrackingEventType.F.name, eventType.getName(), MySqlColumnType.MAX_LENGTH_VARCHAR).setColumnName(F.eventType),
			new LongDateField(F.created, created),
			new StringField(F.notificationId, notificationId, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	/** construct *************************************************************/

	private NotificationTrackingEventKey(){
		this(NotificationTrackingEventType.createEmptyInstance(), null);
	}

	public NotificationTrackingEventKey(NotificationTrackingEventType eventType, String notificationId){
		this.eventType = eventType;
		this.created = new Date();
		this.notificationId = notificationId;
	}

	/** get/set ***************************************************************/

}
