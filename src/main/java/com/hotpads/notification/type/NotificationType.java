package com.hotpads.notification.type;

import java.util.List;

import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.tracking.TrackingNotificationType;

public interface NotificationType extends TrackingNotificationType{

	String getName();

	Class<? extends NotificationTimingStrategy> getTimingStrategyClass();

	boolean isMergeableWith(NotificationType that);

	void makeSendersAndTemplates();

	List<Class<? extends NotificationTemplate>> getTemplates();

}
