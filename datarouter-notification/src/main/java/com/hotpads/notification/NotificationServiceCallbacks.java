package com.hotpads.notification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.notification.tracking.NotificationTrackingEventType;
import com.hotpads.notification.type.NotificationType;

//TODO some of these signatures are definitely ugly and should be temporary
//TODO also, some could/should be combined (maybe just one filtering method on client side?
//TODO also, some batching/asynchronicity should be a part of this once it's actually over the wire
public interface NotificationServiceCallbacks{
	//from NotificationType
	List<NotificationRequest> filterOutIrrelevantNotificationRequests(NotificationType type,
			List<NotificationRequest> originalRequests);
	String getDescription(NotificationType type, NotificationItemLog notificationItemLog);//TODO is this really worth waiting for a request?
	void onSuccess(NotificationType type, List<NotificationRequest> requests);
	default void onTrackingEvent(NotificationType type, NotificationTrackingEventType eventType,
			NotificationLog notificationLog){}

	//other services
	List<NotificationDestination> getActiveDestinations(NotificationUserId userId,
			Collection<NotificationDestinationApp> apps);
	Map<NotificationDestination,String> filterOutOptedOut(NotificationType notificationType,
			List<NotificationDestination> destinations, Map<NotificationDestinationApp,String> appToTemplateMap);
	void removeDisabledSearchDestinationApps(NotificationType notificationType,
			Map<NotificationDestinationApp,String> appToTemplateMap);
}
