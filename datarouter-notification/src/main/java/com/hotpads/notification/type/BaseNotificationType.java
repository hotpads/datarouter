package com.hotpads.notification.type;

import java.util.List;

import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationRequest;

public abstract class BaseNotificationType implements NotificationType{

	@Override
	public List<NotificationRequest> filterOutIrrelevantNotificationRequests(
			List<NotificationRequest> originalRequests){
		return originalRequests;
	}

	@Override
	public String getDescription(NotificationItemLog notificationItemLog){
		return getClass().getName();
	}

	@Override
	public void onSuccess(List<NotificationRequest> requests){}

}
