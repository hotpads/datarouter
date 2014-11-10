package com.hotpads.notification.sender.template;

import java.util.List;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.sender.NotificationSender;

public interface NotificationTemplate {

	Class<? extends NotificationSender> getNotificationSender();

	void setRequests(List<NotificationRequest> requests);

	void setNotificationId(String notificationId);

}
