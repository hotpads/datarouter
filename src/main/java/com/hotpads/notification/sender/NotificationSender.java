package com.hotpads.notification.sender;

import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.sender.template.NotificationTemplate;

public abstract class NotificationSender {
	
	protected NotificationUserId userId;

	public abstract void setTemplate(NotificationTemplate<?> template);

	public void setUserId(NotificationUserId userId) {
		this.userId = userId;
	}
	
	public abstract boolean send() throws RuntimeException;

}
