package com.hotpads.notification.sender;

import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.result.NotificationSendingResult;
import com.hotpads.notification.sender.template.NotificationTemplate;

public abstract class NotificationSender{

	protected NotificationUserId userId;
	protected NotificationDestination notificationDestination;

	public abstract void setTemplate(NotificationTemplate template);

	public void setUserId(NotificationUserId userId, NotificationDestination notificationDestination){
		this.userId = userId;
		this.notificationDestination = notificationDestination;
	}

	public abstract boolean send(NotificationSendingResult result);

}
