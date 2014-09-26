package com.hotpads.notification.databean;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.notification.type.NotificationType;

public class NotificationRequest extends BaseDatabean<NotificationRequestKey,NotificationRequest> {	

	private static final int
		LENGHT_type = MySqlColumnType.MAX_LENGTH_VARCHAR,
		LENGHT_data = MySqlColumnType.MAX_LENGTH_VARCHAR,
		LENGHT_channel = MySqlColumnType.MAX_LENGTH_VARCHAR;

	/****************** fields **************************/

	private NotificationRequestKey key;
	private String type;
	private String data;
	private String channel;
	private Date sentAtDate;

	private static class F {
		private static final String
		type = "type",
		data = "data",
		channel = "channel",
		sentAtDate = "sentAtDate";
	}

	public static class NotificationRequestFielder extends BaseDatabeanFielder<NotificationRequestKey, NotificationRequest> {

		NotificationRequestFielder() {}

		@Override
		public Class<NotificationRequestKey> getKeyFielderClass() {
			return NotificationRequestKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationRequest d) {
			return FieldTool.createList(
					new StringField(F.type, d.type, LENGHT_type),
					new StringField(F.data, d.data, LENGHT_data),
					new StringField(F.channel, d.channel, LENGHT_channel),
					new DateField(F.sentAtDate, d.sentAtDate));
		}
	}

	/************************** construct *************************/

	NotificationRequest() {
		key = new NotificationRequestKey(null, null);
	}

	private NotificationRequest(NotificationUserId userId, Long sentAtMs , String type, String data, String channel) {
		key = new NotificationRequestKey(userId, sentAtMs);
		this.type = type;
		this.data = data;
		this.channel = channel;
		this.sentAtDate = new Date(sentAtMs);
	}

	public NotificationRequest(NotificationUserId userId, String type, String data, String channel) {
		this(userId, System.currentTimeMillis(), type, data, channel);
	}

	public NotificationRequest(NotificationUserId userId, Class<? extends NotificationType> type, String data, String channel) {
		this(userId, type.getName(), data, channel);
	}

	public NotificationRequest(NotificationUserId userId, NotificationType type, String data, String channel) {
		this(userId, type.getName(), data, channel);
	}

	@Override
	public Class<NotificationRequestKey> getKeyClass() {
		return NotificationRequestKey.class;
	}

	/********************* getters / setters **************************/

	@Override
	public NotificationRequestKey getKey() {
		return key;
	}

	public void setKey(NotificationRequestKey key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public String getShortType() {
		String[] tab = type.split("\\.");
		return tab[tab.length - 1];
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setType(NotificationType type) {
		this.type = type.getName();
	}

	public void setType(Class<NotificationType> clazz) {
		this.type = clazz.getName();
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Date getSentAtDate(){
		return sentAtDate;
	}

	@Override
	public String toString() {
		return "NotificationRequest(" + getKey() + ", " + getType() + ", " + getData() + ")";
	}

}
