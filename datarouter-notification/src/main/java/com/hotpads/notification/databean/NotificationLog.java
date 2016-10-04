package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;

public class NotificationLog extends BaseDatabean<NotificationLogKey,NotificationLog>{

	private static final int LENGTH_id = 36; // 32 char + 4 dash

	private NotificationLogKey key;

	private Date created;
	private String type;
	private List<String> itemIds;
	private String channel;
	private String id;

	public static class FieldKeys{
		public static final DateFieldKey created = new DateFieldKey("created");
		public static final StringFieldKey type = new StringFieldKey("type");
		public static final DelimitedStringArrayFieldKey itemIds = new DelimitedStringArrayFieldKey("itemIds", ",");
		public static final StringFieldKey channel = new StringFieldKey("channel");
		public static final StringFieldKey id = new StringFieldKey("id").withSize(LENGTH_id);
	}

	public static class NotificationLogFielder extends BaseDatabeanFielder<NotificationLogKey, NotificationLog>{

		public NotificationLogFielder(){
			super(NotificationLogKey.class);
		}

		@Override
		public Class<NotificationLogKey> getKeyFielderClass(){
			return NotificationLogKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationLog notificationLog){
			return Arrays.asList(
					new DateField(FieldKeys.created, notificationLog.created),
					new StringField(FieldKeys.type, notificationLog.type),
					new DelimitedStringArrayField(FieldKeys.itemIds, notificationLog.itemIds),
					new StringField(FieldKeys.channel, notificationLog.channel),
					new StringField(FieldKeys.id, notificationLog.id));
		}

		@Override
		public Map<String,List<Field<?>>> getIndexes(NotificationLog notificationLog){
			Map<String,List<Field<?>>> map = new HashMap<>();
			map.put("index_reverseCreatedMs", new NotificationLogByReverseCreatedMsLookup(null).getFields());
			return map;
		}

		@Override
		public MySqlCollation getCollation(){
			return MySqlCollation.utf8_general_ci;
		}

		@Override
		public MySqlRowFormat getRowFormat(){
			return MySqlRowFormat.COMPACT;
		}

	}

	public static class NotificationLogByReverseCreatedMsLookup extends BaseLookup<NotificationLogKey>{

		private Long reverseCreatedMs;

		public NotificationLogByReverseCreatedMsLookup(Long reverseCreatedMs){
			this.reverseCreatedMs = reverseCreatedMs;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
					new LongField(NotificationLogKey.FieldKeys.reverseCreatedMs, reverseCreatedMs),
					new StringEnumField<>(NotificationUserId.FieldKeys.userType, null, BaseNotificationUserIdEntityKey
							.PREFIX_userId),
					new StringField(BaseNotificationUserIdEntityKey.PREFIX_userId, NotificationUserId.FieldKeys.userId,
							null),
					new StringField(NotificationLogKey.FieldKeys.template, null));
		}

	}

	public NotificationLog() {
		this(new NotificationUserId(null, null), null, null, null, null, null, null);
	}

	public NotificationLog(NotificationUserId userId, Date created, String template,
			String type, List<String> itemIds, String channel, String id){
		this.key = new NotificationLogKey(userId, created, template);
		this.created = created;
		this.type = type;
		this.itemIds = itemIds;
		this.channel = channel;
		this.id = id;
	}

	@Override
	public Class<NotificationLogKey> getKeyClass() {
		return NotificationLogKey.class;
	}

	@Override
	public NotificationLogKey getKey() {
		return key;
	}

	public Date getCreated(){
		return created;
	}

	public String getType(){
		return type;
	}

	public List<String> getItemIds() {
		return itemIds;
	}

	public void setItemIds(List<String> itemIds) {
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
