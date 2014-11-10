package com.hotpads.notification.type;

import java.util.List;

import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.timing.NotificationTimingStrategy;

public interface NotificationType {

	String getName();

	Class<? extends NotificationTimingStrategy> getTimingStrategyClass();

	boolean isMergeableWith(NotificationType that);

	void makeSendersAndTemplates();

	List<Class<? extends NotificationTemplate>> getTemplates();

}
