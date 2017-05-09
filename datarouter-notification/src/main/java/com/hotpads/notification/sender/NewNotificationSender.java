package com.hotpads.notification.sender;

import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.result.NotificationSendingResult;
import com.hotpads.notification.sender.template.BaseBuiltTemplate;

public interface NewNotificationSender{
	//TODO would make sense to use notificationResult instead of returning true/false (too simple to have meaning)
	boolean send(BaseBuiltTemplate template, NotificationDestination notificationDestination,
			NotificationUserId notificationUserId, NotificationSendingResult notificationResult);
}
