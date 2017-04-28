package com.hotpads.notification.tracking;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationTrackingEventKey extends BasePrimaryKey<NotificationTrackingEventKey>{

	/** fields ****************************************************************/

	private NotificationTrackingEventType eventType;
	private Date created;
	private String notificationId;

	/** columns ***************************************************************/

	public static class F{
		public static final String
			eventType = "eventType",
			created = "created",
			notificationId = "notificationId";
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(F.eventType, NotificationTrackingEventType.F.name, F.eventType, true, eventType.name,
						MySqlColumnType.DEFAULT_LENGTH_VARCHAR),
				new LongDateField(F.created, created),
				new StringField(F.notificationId, notificationId, MySqlColumnType.DEFAULT_LENGTH_VARCHAR));
	}

	/** construct *************************************************************/

	@SuppressWarnings("unused") // dr reflection
	private NotificationTrackingEventKey(){
		this(NotificationTrackingEventType.createEmptyInstance(), null);
	}

	public NotificationTrackingEventKey(NotificationTrackingEventType eventType, String notificationId){
		this(eventType, new Date(), notificationId);
	}

	public NotificationTrackingEventKey(NotificationTrackingEventType eventType, Date created, String notificationId){
		this.eventType = eventType;
		this.created = created;
		this.notificationId = notificationId;
	}

	/** get/set ***************************************************************/

	public NotificationTrackingEventType getEventType(){
		return eventType;
	}

	public Date getCreated(){
		return created;
	}

	public String getNotificationId(){
		return notificationId;
	}

}
