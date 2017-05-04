package com.hotpads.notification;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationAppName;
import com.hotpads.notification.sender.template.NotificationTemplateRequest;
import com.hotpads.notification.sender.template.NotificationTemplateResponse;

//TODO some batching/asynchronicity should probably be a part of this once it's actually over web
public interface NotificationServiceCallbacks{
	//from NotificationType
	List<NotificationRequest> filterOutIrrelevantNotificationRequests(String notificationTypeString,
			List<NotificationRequest> originalRequests);
	void onSuccess(String notificationTypeString, List<NotificationRequest> requests);

	//other services
	Set<NotificationDestination> getActiveDestinations(NotificationUserId userId,
			Collection<NotificationDestinationAppName> apps);
	Set<NotificationDestinationAppName> filterOutDisabledDestinationApps(String notificationTypeString,
			Set<NotificationDestinationAppName> destinationApps);

	//template building
	NotificationTemplateResponse buildRequests(NotificationTemplateRequest request);
}
