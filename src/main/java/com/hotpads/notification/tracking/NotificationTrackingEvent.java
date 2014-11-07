package com.hotpads.notification.tracking;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;


/** CREATE SCRIPT
com.hotpads.notification.tracking.NotificationTrackingEvent{
  PK{
    StringEnumField<TrackingEventType> type,
    LongDateField created,
    StringField notificationId
  }
  StringField TrackingNotificationType,
  StringField source

}

*/
public class NotificationTrackingEvent extends BaseDatabean<NotificationTrackingEventKey,NotificationTrackingEvent> {

	/** fields ****************************************************************/

	private NotificationTrackingEventKey key;

	private String trackingNotificationType;
	private String source;


	/** columns ***************************************************************/

	public static class F {
		public static final String
			trackingNotificationType = "trackingNotificationType",
			source = "source";
	}

	/** fielder ***************************************************************/

	public static class NotificationTrackingEventFielder extends
			BaseDatabeanFielder<NotificationTrackingEventKey,NotificationTrackingEvent>{

		private NotificationTrackingEventFielder(){}

		@Override
		public Class<NotificationTrackingEventKey> getKeyFielderClass() {
			return NotificationTrackingEventKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTrackingEvent d){
			return FieldTool.createList(
				new StringField(F.trackingNotificationType, d.trackingNotificationType, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.source, d.source, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

	/** construct *************************************************************/

	public NotificationTrackingEvent(){
		this(NotificationTrackingEventType.NULL, null, null);

	}

	public NotificationTrackingEvent(NotificationTrackingEventType type, Date created, String notificationId){
		this.key = new NotificationTrackingEventKey(type, created, notificationId);
	}

	/** databean **************************************************************/

	@Override
	public Class<NotificationTrackingEventKey> getKeyClass() {
		return NotificationTrackingEventKey.class;
	}

	@Override
	public NotificationTrackingEventKey getKey() {
		return key;
	}

	/** get/set ***************************************************************/

}

