package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class NotificationItemLog extends BaseDatabean<NotificationItemLogKey,NotificationItemLog>{

	/****************** fields **************************/

	private NotificationItemLogKey key;
	private Date created;
	private String channel;
	private String notificationIds;

	public static class FieldKeys{
    	public static final DateFieldKey created = new DateFieldKey("created");
    	public static final StringFieldKey channel = new StringFieldKey("channel");
    	public static final StringFieldKey notificationIds = new StringFieldKey("notificationIds");
}

	public static class NotificationItemLogFielder
	extends BaseDatabeanFielder<NotificationItemLogKey, NotificationItemLog>{

		public NotificationItemLogFielder(){
			super(NotificationItemLogKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationItemLog notificationItemLog){
			return Arrays.asList(
					new DateField(FieldKeys.created, notificationItemLog.created),
					new StringField(FieldKeys.channel, notificationItemLog.channel),
					new StringField(FieldKeys.notificationIds, notificationItemLog.notificationIds));
		}

		@Override
		public MySqlRowFormat getRowFormat(){
			return MySqlRowFormat.COMPACT;
		}

	}

	/************************** construct *************************/

	public NotificationItemLog(){
		this.key = new NotificationItemLogKey();
	}

	public NotificationItemLog(NotificationRequest request, String notificationIds){
		this.created = new Date();
		this.key = new NotificationItemLogKey(request.getKey().getNotificationUserId(), created, request.getType(),
				request.getData());
		this.channel = request.getChannel();
		this.notificationIds = notificationIds;
	}

	@Override
	public Class<NotificationItemLogKey> getKeyClass(){
		return NotificationItemLogKey.class;
	}

	/*********************** get/set ************************************/

	@Override
	public NotificationItemLogKey getKey(){
		return key;
	}

	public Date getCreated(){
		return created;
	}

	public String getChannel(){
		return channel;
	}

}
