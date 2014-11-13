package com.hotpads.notification.databean;

import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.util.core.ObjectTool;

@SuppressWarnings("serial")
public class NotificationLogKey extends BasePrimaryKey<NotificationLogKey> {

	private static final int 
		LENGTH_userType = MySqlColumnType.MAX_LENGTH_VARCHAR,
		LENGTH_userId = MySqlColumnType.MAX_LENGTH_VARCHAR,
		LENGTH_template = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	private NotificationUserType userType;
	private String userId;
	private Long reverseCreatedMs;
	private String template;

	private static class F {
		private static final String
				userType = "userType",
				userId = "userId",
				reverseCreatedMs = "reverseCreatedMs",
				template = "template";
	}

	NotificationLogKey() {}
	
	public NotificationLogKey(NotificationUserId userId, Date created, String template) {
		this(userId, getReverseDate(created), template);
	}

	public NotificationLogKey(NotificationUserId userId, Long reverseCreatedMs, String template) {
		this(userId);
		this.reverseCreatedMs = reverseCreatedMs;
		this.template = template;
	}

	public NotificationLogKey(NotificationUserId userId){
		this.userType = ObjectTool.anyNull(userId) ? null : userId.getType();
		this.userId = ObjectTool.anyNull(userId) ? null : userId.getId();
	}

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(
				new StringEnumField<>(NotificationUserType.class, F.userType, userType, LENGTH_userType),
				new StringField(F.userId, userId, LENGTH_userId),
				new LongField(F.reverseCreatedMs, reverseCreatedMs),
				new StringField(F.template, template, LENGTH_template)
				);
	}

	/***************************Methods*************************************/

	public static Long getReverseDate(Date date) {
		if (date == null) {
			return null;
		}
		return Long.MAX_VALUE - date.getTime();
	}
	
	/*****************************Getters and Setters**************************/

	public NotificationUserId getNotificationUserId() {
		return new NotificationUserId(userType, userId);
	}

	public NotificationUserType getUserType() {
		return userType;
	}

	public void setUserType(NotificationUserType userType) {
		this.userType = userType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Date getCreated() {
		if (reverseCreatedMs == null) {
			return null;
		}
		return new Date(Long.MAX_VALUE - reverseCreatedMs);
	}

	public void setCreated(Date created) {
		this.reverseCreatedMs = getReverseDate(created);
	}

	public String getTemplate(){
		return template;
	}

}
