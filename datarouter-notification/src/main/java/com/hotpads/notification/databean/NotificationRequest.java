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
import com.hotpads.notification.type.NotificationType;

public class NotificationRequest extends BaseDatabean<NotificationRequestKey, NotificationRequest>{

	private NotificationRequestKey key;

	private String type;
	private String data;
	private String channel;
	private Date sentAtDate;

	public NotificationRequest(){
		this.key = new NotificationRequestKey(new NotificationUserId(null, null), null);
	}

	public NotificationRequest(NotificationUserType userType, String id, Class<? extends NotificationType> type,
			String data, String channel){
		this(new NotificationUserId(userType, id), type, data, channel);
	}

	public NotificationRequest(NotificationUserId userId, Class<? extends NotificationType> type, String data,
			String channel){
		this(userId, type.getName(), data, channel);
	}

	public NotificationRequest(NotificationUserId userId, String type, String data, String channel){
		this(userId, System.currentTimeMillis(), type, data, channel);
	}

	private NotificationRequest(NotificationUserId userId, Long sentAtMs, String type, String data, String channel){
		this.key = new NotificationRequestKey(userId, sentAtMs);
		this.type = type;
		this.data = data;
		this.channel = channel;
		this.sentAtDate = new Date(sentAtMs);
	}

	@Override
	public Class<NotificationRequestKey> getKeyClass(){
		return NotificationRequestKey.class;
	}

	@Override
	public NotificationRequestKey getKey(){
		return key;
	}

	public String getType(){
		return type;
	}

	public String getShortType(){
		String[] tab = type.split("\\.");
		return tab[tab.length - 1];
	}

	public String getData(){
		return data;
	}

	public void setData(String data){
		this.data = data;
	}

	public String getChannel(){
		return channel;
	}

	public Date getSentAtDate(){
		return sentAtDate;
	}

	@Override
	public String toString(){
		return "NotificationRequest(" + key + ", " + type + ", " + data + ")";
	}

	private static class FieldKeys{

		private static final StringFieldKey type = new StringFieldKey("type");
		private static final StringFieldKey data = new StringFieldKey("data");
		private static final StringFieldKey channel = new StringFieldKey("channel");
		private static final DateFieldKey sentAtDate = new DateFieldKey("sentAtDate");
	}

	public static class NotificationRequestFielder
			extends BaseDatabeanFielder<NotificationRequestKey, NotificationRequest>{

		public NotificationRequestFielder(){
			super(NotificationRequestKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationRequest notificationRequest){
			return Arrays.asList(
					new StringField(FieldKeys.type, notificationRequest.type),
					new StringField(FieldKeys.data, notificationRequest.data),
					new StringField(FieldKeys.channel, notificationRequest.channel),
					new DateField(FieldKeys.sentAtDate, notificationRequest.sentAtDate));
		}
	}

}
