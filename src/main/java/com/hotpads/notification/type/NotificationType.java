package com.hotpads.notification.type;

import java.util.List;
import java.util.Map;

import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.tracking.TrackingNotificationType;

public interface NotificationType extends TrackingNotificationType{

	@Override
	String getName();

	Class<? extends NotificationTimingStrategy> getTimingStrategyClass();

	boolean isMergeableWith(NotificationType that);

	Map<NotificationDestinationApp,Class<? extends NotificationTemplate>> getTemplateForApp();

	List<NotificationDestinationApp> getDestinationApps();

}
