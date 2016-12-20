package com.hotpads.notification.tracking;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;

/* CREATE SCRIPT
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

public class NotificationTrackingEvent extends BaseDatabean<NotificationTrackingEventKey,NotificationTrackingEvent>{

	/** fields ****************************************************************/

	private NotificationTrackingEventKey key;

	private TrackingNotificationType trackingNotificationType;
	private String source;


	/** columns ***************************************************************/

	public static final String COL_TYPE = "type";

	public static class F{
		public static final String
			trackingNotificationType = "trackingNotificationType",
			source = "source";
	}

	/** fielder ***************************************************************/

	public static class NotificationTrackingEventFielder
	extends BaseDatabeanFielder<NotificationTrackingEventKey,NotificationTrackingEvent>{

		public NotificationTrackingEventFielder(){
			super(NotificationTrackingEventKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTrackingEvent trackingEvent){
			return Arrays.asList(
				new StringField(F.trackingNotificationType, TrackingNotificationType.F.name, COL_TYPE, false,
						trackingEvent.trackingNotificationType.name, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.source, trackingEvent.source, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}

		@Override
		public MySqlRowFormat getRowFormat(){
			return MySqlRowFormat.COMPACT;
		}

	}

	/** construct *************************************************************/

	public NotificationTrackingEvent(){
		this(NotificationTrackingEventType.createEmptyInstance(), null, new TrackingNotificationType(), null);
	}

	public NotificationTrackingEvent(NotificationTrackingEventType eventType, String notificationId,
			TrackingNotificationType trackingNotificationType, String source){
		this.key = new NotificationTrackingEventKey(eventType, notificationId);
		this.trackingNotificationType = trackingNotificationType;
		this.source = source;
	}

	/** databean **************************************************************/

	@Override
	public Class<NotificationTrackingEventKey> getKeyClass(){
		return NotificationTrackingEventKey.class;
	}

	@Override
	public NotificationTrackingEventKey getKey(){
		return key;
	}

}
