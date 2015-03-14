package com.hotpads.notification.sender;

import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.result.NotificationSendingResult;
import com.hotpads.notification.sender.template.NotificationTemplate;

public abstract class NotificationSender {

	protected NotificationUserId userId;
	protected String deviceId;

	public abstract void setTemplate(NotificationTemplate template);

	public void setUserId(NotificationUserId userId, String deviceId) {
		this.userId = userId;
		this.deviceId = deviceId;
	}

	public abstract boolean send(String deviceId, NotificationSendingResult result) throws RuntimeException;

}
