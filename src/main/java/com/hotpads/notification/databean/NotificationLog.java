package com.hotpads.notification.databean;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseLatin1Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.unique.base.BaseStringUniqueKey;

public class NotificationLog extends BaseDatabean<NotificationLogKey, NotificationLog> {

	private static final int
			LENGTH_type = MySqlColumnType.MAX_LENGTH_VARCHAR,
			LENGTH_channel = MySqlColumnType.MAX_LENGTH_VARCHAR,
			LENGTH_id = 36; // 32 char + 4 dash

	private NotificationLogKey key;
	private Date created;
	private String type;
	private List<String> itemIds;
	private String channel;
	private String id;

	private static class F {
		private static final String
				created = "created",
				type = "type",
				itemIds = "itemIds",
				channel = "channel",
				id = "id";
	}

	public static class NotificationLogFielder extends BaseLatin1Fielder<NotificationLogKey, NotificationLog>{

		private NotificationLogFielder(){}

		@Override
		public Class<NotificationLogKey> getKeyFielderClass(){
			return NotificationLogKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationLog d){
			return FieldTool.createList(
					new DateField(F.created, d.created),
					new StringField(F.type, d.type, LENGTH_type),
					new DelimitedStringArrayField(F.itemIds, ",", d.itemIds),
					new StringField(F.channel, d.channel, LENGTH_channel),
					new StringField(F.id, d.id, LENGTH_id)
					);
		}

		@Override
		public Map<String,List<Field<?>>> getIndexes(NotificationLog d){
			Map<String,List<Field<?>>> map = new HashMap<>();
			map.put("index_notificationId", FieldTool.createList(
					new StringField(F.id, d.id, LENGTH_id)));
			map.put("index_reverseCreatedMs", new NotificationLogByReverseCreatedMsLookup().getFields());
			return map;
		}
	}

	public static class NotificationLogByReverseCreatedMsLookup extends BaseLookup<NotificationLogKey>{

		private Long reverseCreatedMs;

		NotificationLogByReverseCreatedMsLookup(){
			this(null);
		}

		public NotificationLogByReverseCreatedMsLookup(Long reverseCreatedMs){
			this.reverseCreatedMs = reverseCreatedMs;
			this.setPrimaryKey(new NotificationLogKey(null, reverseCreatedMs, null));
		}

		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList(
					new LongField(NotificationLogKey.F.reverseCreatedMs, getPrimaryKey().getReverseCreatedMs()),
					new StringEnumField<>(NotificationUserType.class, NotificationLogKey.F.userType, getPrimaryKey().getUserType(), NotificationLogKey.LENGTH_userType),
					new StringField(NotificationLogKey.F.userId, getPrimaryKey().getUserId(), NotificationLogKey.LENGTH_userId),
					new StringField(NotificationLogKey.F.template, getPrimaryKey().getTemplate(), NotificationLogKey.LENGTH_template)
					);
		}

	}

	@SuppressWarnings("serial")
	public static class NotificationIdLookup extends BaseStringUniqueKey<NotificationLogKey>{

		public NotificationIdLookup(String key){
			super(key);
		}

		@Override
		public List<Field<?>> getFields(){
			return FieldTool.createList(
					new StringField(F.id, id, LENGTH_id));
		}

	}

	private NotificationLog() {
		key = new NotificationLogKey();
	}

	public NotificationLog(NotificationUserId userId, Date created, String template,
			String type, List<String> itemIds, String channel, String id){
		key = new NotificationLogKey(userId, created, template);
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

	public void setKey(NotificationLogKey key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getChannel(){
		return channel;
	}

}
