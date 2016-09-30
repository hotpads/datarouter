package com.hotpads.notification.log;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationLogKey;
import com.hotpads.notification.databean.NotificationUserId;

public class NotificationLogById
extends BaseDatabean<NotificationLogByIdKey,NotificationLogById>
implements UniqueIndexEntry<NotificationLogByIdKey,NotificationLogById,NotificationLogKey,NotificationLog>{

	public static final String PREFIX_userId = "userId";

	private NotificationLogByIdKey key;
	private NotificationUserId userId;
	private Long reverseCreatedMs;
	private String template;

	public static class FieldKeys{
		public static final LongFieldKey reverseCreatedMs = new LongFieldKey("reverseCreatedMs");
		public static final StringFieldKey template = new StringFieldKey("template");
	}

	public static class NotificationLogByIdFielder
	extends BaseDatabeanFielder<NotificationLogByIdKey,NotificationLogById>{

		public NotificationLogByIdFielder(){
			super(NotificationLogByIdKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationLogById databean){
			return Arrays.asList(
					new StringEnumField<>(NotificationUserId.FieldKeys.userType, databean.userId.getType(),
							PREFIX_userId),
					new StringField(PREFIX_userId, NotificationUserId.FieldKeys.userId, databean.userId.getId()),
					new LongField(FieldKeys.reverseCreatedMs, databean.reverseCreatedMs),
					new StringField(FieldKeys.template, databean.template));
		}

	}

	public NotificationLogById(){
		this(null);
	}

	public NotificationLogById(String id){
		this(id, new NotificationUserId(null, null), null, null);
	}

	public NotificationLogById(String id, NotificationUserId userId,Long reverseCreatedMs, String template){
		this.key = new NotificationLogByIdKey(id);
		this.userId = userId;
		this.reverseCreatedMs = reverseCreatedMs;
		this.template = template;
	}

	@Override
	public Class<NotificationLogByIdKey> getKeyClass(){
		return NotificationLogByIdKey.class;
	}

	@Override
	public NotificationLogByIdKey getKey(){
		return key;
	}

	@Override
	public NotificationLogKey getTargetKey(){
		return new NotificationLogKey(userId, reverseCreatedMs, template);
	}

	@Override
	public List<NotificationLogById> createFromDatabean(NotificationLog target){
		return Arrays.asList(new NotificationLogById(target.getId(), target.getKey().getEntityKey().getUserId(),
				target.getKey().getReverseCreatedMs(), target.getKey().getTemplate()));
	}

}
