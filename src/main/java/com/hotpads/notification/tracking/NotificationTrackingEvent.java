package com.hotpads.notification.tracking;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseLatin1Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.notification.type.BaseNotificationType;


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

	private TrackingNotificationType trackingNotificationType;
	private String source;


	/** columns ***************************************************************/

	public static class F {
		public static final String
			trackingNotificationType = "trackingNotificationType",
			source = "source";
	}

	/** fielder ***************************************************************/

	public static class NotificationTrackingEventFielder extends
	BaseLatin1Fielder<NotificationTrackingEventKey,NotificationTrackingEvent>{

		private NotificationTrackingEventFielder(){}

		@Override
		public Class<NotificationTrackingEventKey> getKeyFielderClass() {
			return NotificationTrackingEventKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTrackingEvent d){
			return FieldTool.createList(
				new StringField(F.trackingNotificationType, d.trackingNotificationType.getFieldName(), d.trackingNotificationType.getName(), MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.source, d.source, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

	}

	/** construct *************************************************************/

	private NotificationTrackingEvent(){
		this(NotificationTrackingEventType.createEmptyInstance(), null, BaseNotificationType.createEmptyInstance(), null);
	}

	public NotificationTrackingEvent(NotificationTrackingEventType eventType, String notificationId, TrackingNotificationType trackingNotificationType, String source){
		this.key = new NotificationTrackingEventKey(eventType, notificationId);
		this.trackingNotificationType = trackingNotificationType;
		this.source = source;
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

