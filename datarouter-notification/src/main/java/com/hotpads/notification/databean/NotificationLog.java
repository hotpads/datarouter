package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayFieldKey;

public class NotificationLog extends BaseDatabean<NotificationLogKey,NotificationLog>{

	private static final int LENGTH_id = 36; // 32 char + 4 dash

	private NotificationLogKey key;

	private Date created;
	private String type;
	private List<String> itemIds;
	private String channel;
	private String id;
	private String deviceId;

	public static class FieldKeys{
		public static final DateFieldKey created = new DateFieldKey("created");
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final DelimitedStringArrayFieldKey itemIds = new DelimitedStringArrayFieldKey("itemIds", ",");
		public static final StringFieldKey channel = new StringFieldKey("channel");
		public static final StringFieldKey id = new StringFieldKey("id").withSize(LENGTH_id);
		public static final StringFieldKey deviceId = new StringFieldKey("deviceId");
	}

	public static class NotificationLogFielder extends BaseDatabeanFielder<NotificationLogKey, NotificationLog>{

		public NotificationLogFielder(){
			super(NotificationLogKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationLog notificationLog){
			return Arrays.asList(
					new DateField(FieldKeys.created, notificationLog.created),
					new StringField(FieldKeys.type, notificationLog.type),
					new DelimitedStringArrayField(FieldKeys.itemIds, notificationLog.itemIds),
					new StringField(FieldKeys.channel, notificationLog.channel),
					new StringField(FieldKeys.id, notificationLog.id),
					new StringField(FieldKeys.deviceId, notificationLog.deviceId));
		}

	}

	public NotificationLog(){
		this(new NotificationUserId(null, null), null, null, null, null, null, null, null);
	}

	public NotificationLog(NotificationUserId userId, Date created, String template,
			String type, List<String> itemIds, String channel, String id, String deviceId){
		this.key = new NotificationLogKey(userId, created, template);
		this.created = created;
		this.type = type;
		this.itemIds = itemIds;
		this.channel = channel;
		this.id = id;
		this.deviceId = deviceId;
	}

	@Override
	public Class<NotificationLogKey> getKeyClass(){
		return NotificationLogKey.class;
	}

	@Override
	public NotificationLogKey getKey(){
		return key;
	}

	public Date getCreated(){
		return created;
	}

	public String getType(){
		return type;
	}

	public List<String> getItemIds(){
		return itemIds;
	}

	public void setItemIds(List<String> itemIds){
		this.itemIds = itemIds;
	}

	public String getId(){
		return id;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getChannel(){
		return channel;
	}

}
