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
import com.hotpads.notification.itemlog.NotificationItemLogEntityKey;

public class NotificationItemLogKey extends BaseEntityPrimaryKey<NotificationItemLogEntityKey,NotificationItemLogKey>{

	private NotificationItemLogEntityKey entityKey;

	private Long reverseCreatedMs;
	private String notificationType;
	private String data;

	public static class FieldKeys{
	    	public static final LongFieldKey reverseCreatedMs = new LongFieldKey("reverseCreatedMs");
	    	public static final StringFieldKey notificationType = new StringFieldKey("notificationType");
	    	public static final StringFieldKey data = new StringFieldKey("data");
   }

	public NotificationItemLogKey(){
		this(new NotificationUserId(null, null), null, null, null);
	}

	public NotificationItemLogKey(NotificationUserId userId, Date created, String type, String data){
		this.entityKey = new NotificationItemLogEntityKey(userId);
		this.reverseCreatedMs = DrDateTool.toReverseDateLong(created);
		this.notificationType = type;
		this.data = data;
	}

	@Override
	public NotificationItemLogEntityKey getEntityKey(){
		return entityKey;
	}

	@Override
	public NotificationItemLogKey prefixFromEntityKey(NotificationItemLogEntityKey entityKey){
		return new NotificationItemLogKey(entityKey.getUserId(), null, null, null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields() {
		return Arrays.asList(
				new LongField(FieldKeys.reverseCreatedMs, reverseCreatedMs),
				new StringField(FieldKeys.notificationType, notificationType),
				new StringField(FieldKeys.data, data));
	}

	public String getNotificationType(){
		return notificationType;
	}

	public String getData(){
		return data;
	}

	public Long getReverseCreatedMs(){
		return reverseCreatedMs;
	}

}
