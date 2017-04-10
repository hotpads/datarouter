package com.hotpads.notification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationAppName;
import com.hotpads.notification.tracking.NotificationTrackingEventType;

//TODO some of these signatures are definitely ugly and should be temporary
//TODO also, some could/should be combined (maybe just one filtering method on client side?
//TODO also, some batching/asynchronicity should be a part of this once it's actually over the wire
public interface NotificationServiceCallbacks{
	//from NotificationType
	List<NotificationRequest> filterOutIrrelevantNotificationRequests(String notificationTypeString,
			List<NotificationRequest> originalRequests);
	String getDescription(String notificationTypeString, NotificationItemLog notificationItemLog);//TODO is this really worth waiting for a request?
	void onSuccess(String notificationTypeString, List<NotificationRequest> requests);
	default void onTrackingEvent(String notificationTypeString, NotificationTrackingEventType eventType,
			NotificationLog notificationLog){}

	//other services
	List<NotificationDestination> getActiveDestinations(NotificationUserId userId,
			Collection<NotificationDestinationAppName> apps);
//	List<NotificationDestination> getDestinations(Collection<NotificationDestinationAppName> apps,
//			NotificationUserId userId);
	void removeDisabledSearchDestinationApps(String notificationTypeString,
			Map<NotificationDestinationAppName,String> appToTemplateMap);
}
