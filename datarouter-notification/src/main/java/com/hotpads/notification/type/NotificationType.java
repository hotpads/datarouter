package com.hotpads.notification.type;

import java.util.List;

import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.tracking.NotificationTrackingEventType;

public interface NotificationType{
	List<NotificationRequest> filterOutIrrelevantNotificationRequests(List<NotificationRequest> originalRequests);

	String getDescription(NotificationItemLog notificationItemLog);

	void onSuccess(List<NotificationRequest> requests);

	default void onTrackingEvent(NotificationTrackingEventType eventType, NotificationLog notificationLog){}
}
