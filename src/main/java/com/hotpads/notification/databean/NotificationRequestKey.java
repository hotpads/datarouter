package com.hotpads.notification.databean;

import java.util.List;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.databean.NotificationRequest.F;

@SuppressWarnings("serial")
public class NotificationRequestKey extends BasePrimaryKey<NotificationRequestKey> {

	private static final int
			LENGTH_userType = MySqlColumnType.MAX_LENGTH_VARCHAR,
			LENGTH_userId = MySqlColumnType.MAX_LENGTH_VARCHAR;

	private NotificationUserType userType;
	private String userId;
	private Long sentAtMs;
	private Integer nanoTime;

	NotificationRequestKey(){
	}

	public NotificationRequestKey(NotificationUserId userId, Long sendAtMs) {
		this.userType = userId == null ? null : userId.getType();
		this.userId = userId == null ? null : userId.getId();
		this.sentAtMs = sendAtMs;
		this.nanoTime = (int) System.nanoTime();
	}

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(
				new StringEnumField<NotificationUserType>(NotificationUserType.class, F.userType, userType, LENGTH_userType),
				new StringField(F.userId, userId, LENGTH_userId),
				new LongField(F.sentAtMs, sentAtMs),
				new IntegerField(F.nanoTime, nanoTime));
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

	public Long getSentAtMs() {
		return sentAtMs;
	}

	public void setSentAtMs(Long sendAtMs) {
		this.sentAtMs = sendAtMs;
	}

	public String toString() {
		return "NotificationRequestKey (" + userType + ", " + userId + ", " + sentAtMs + ")";
	}

	public static class Tester {

		@Test
		public void nanoTest() {
			System.out.println(new NotificationRequestKey(null, System.currentTimeMillis()).nanoTime);
			System.out.println(new NotificationRequestKey(null, System.currentTimeMillis()).nanoTime);
		}
	}
}
