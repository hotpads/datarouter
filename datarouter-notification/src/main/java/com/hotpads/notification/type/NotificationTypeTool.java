package com.hotpads.notification.type;

import java.util.List;
import java.util.stream.Collectors;

import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;

public class NotificationTypeTool{

	public static List<NotificationDestination> getDestinations(NotificationType notificationType,
			NotificationUserId userId){
		return notificationType.getTemplateForApp().keySet().stream()
				.filter(app -> app.accept(userId.getType()))
				.map(app -> new NotificationDestination(null, app, userId.getId()))
				.collect(Collectors.toList());
	}

}
