package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;
import com.hotpads.notification.request.NotificationRequestEntityKey;

public class NotificationRequestKey extends BaseEntityPrimaryKey<NotificationRequestEntityKey,NotificationRequestKey> {

	private NotificationRequestEntityKey entityKey;

	private Long sentAtMs;
	private Integer nanoTime;

	private static class FieldKeys{
		private static final LongFieldKey sentAtMs = new LongFieldKey("sentAtMs");
		private static final IntegerFieldKey nanoTime = new IntegerFieldKey("nanoTime");
	}

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationRequestKey(){
		this(new NotificationUserId(null, null), null);
	}

	public NotificationRequestKey(NotificationUserId userId, Long sendAtMs){
		this.entityKey = new NotificationRequestEntityKey(userId);
		this.sentAtMs = sendAtMs;
		this.nanoTime = (int)System.nanoTime();
	}

	@Override
	public NotificationRequestEntityKey getEntityKey(){
		return entityKey;
	}

	@Override
	public NotificationRequestKey prefixFromEntityKey(NotificationRequestEntityKey entityKey){
		return new NotificationRequestKey(entityKey.getUserId(), null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(
				new LongField(FieldKeys.sentAtMs, sentAtMs),
				new IntegerField(FieldKeys.nanoTime, nanoTime));
	}

	public NotificationUserId getNotificationUserId() {
		return entityKey.getUserId();
	}

	public NotificationUserType getUserType() {
		return entityKey.getUserId().getType();
	}

	public String getUserId() {
		return entityKey.getUserId().getId();
	}

	public Long getSentAtMs() {
		return sentAtMs;
	}

	public void setSentAtMs(Long sendAtMs) {
		this.sentAtMs = sendAtMs;
	}

}
