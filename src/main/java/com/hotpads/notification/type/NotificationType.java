package com.hotpads.notification.type;

import java.util.Map;

import com.hotpads.notification.sender.NotificationSender;
import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.timing.NotificationTimingStrategy;

public interface NotificationType {

	String getName();

	int getMaxItems();

	Class<? extends NotificationTimingStrategy> getTimingStrategyClass();

	boolean isMergeableWith(NotificationType that);

	void makeSendersAndTemplates();

	<S extends NotificationSender> Map<Class<S>, Class<? extends NotificationTemplate<S>>> getSendersAndTemplates();

}
