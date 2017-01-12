package com.hotpads.notification.type;

import java.util.List;
import java.util.Map;

import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.notification.destination.NotificationDestinationAppEnum;
import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.tracking.NotificationTrackingEventType;

public interface NotificationType{

	String getName();

	boolean isMergeableWith(NotificationType that);

	Map<NotificationDestinationAppEnum,Class<? extends NotificationTemplate>> getTemplateForApp();

	List<NotificationDestinationApp> getDestinationApps();

	List<NotificationRequest> filterOutIrrelevantNotificationRequests(List<NotificationRequest> originalRequests);

	String getDescription(NotificationItemLog notificationItemLog);

	void onSuccess(List<NotificationRequest> requests);

	default void onTrackingEvent(NotificationTrackingEventType eventType, NotificationLog notificationLog){}

}
