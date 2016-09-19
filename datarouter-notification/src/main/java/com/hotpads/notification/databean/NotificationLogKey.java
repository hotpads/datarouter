package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.notification.log.NotificationLogEntityKey;

@SuppressWarnings("serial")
public class NotificationLogKey extends BaseEntityPrimaryKey<NotificationLogEntityKey,NotificationLogKey>{

	private NotificationLogEntityKey entityKey;

	private Long reverseCreatedMs;
	private String template;

	public static class FieldKeys{
		public static final LongFieldKey reverseCreatedMs = new LongFieldKey("reverseCreatedMs");
		public static final StringFieldKey template = new StringFieldKey("template");
	}

	@SuppressWarnings("unused") // used by datarouter reflextion
	private NotificationLogKey(){
		this(new NotificationUserId(null, null));
	}

	public NotificationLogKey(NotificationUserId userId, Date created, String template) {
		this(userId, DrDateTool.toReverseDateLong(created), template);
	}

	public NotificationLogKey(NotificationUserId userId, Long reverseCreatedMs, String template) {
		this(userId);
		this.reverseCreatedMs = reverseCreatedMs;
		this.template = template;
	}

	public NotificationLogKey(NotificationUserId userId){
		this.entityKey = new NotificationLogEntityKey(userId);
	}

	@Override
	public NotificationLogEntityKey getEntityKey(){
		return entityKey;
	}

	@Override
	public NotificationLogKey prefixFromEntityKey(NotificationLogEntityKey entityKey){
		return new NotificationLogKey(entityKey.getUserId());
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(
				new LongField(FieldKeys.reverseCreatedMs, reverseCreatedMs),
				new StringField(FieldKeys.template, template));
	}

	public Long getReverseCreatedMs(){
		return reverseCreatedMs;
	}

	public Date getCreated(){
		return DrDateTool.fromReverseDateLong(reverseCreatedMs);
	}

	public String getTemplate(){
		return template;
	}

}
