package com.hotpads.notification.type;

import java.util.List;

import com.hotpads.notification.sender.SenderAndTemplate;
import com.hotpads.notification.strategy.NotificationTimingStrategy;

public interface NotificationType {

	String getName();

	int getMaxItems();

	NotificationTimingStrategy getTimingStrategy();

	boolean isMergeableWith(NotificationType that);

	void makeSendersAndTemplates();
	
	List<SenderAndTemplate<?>> getSendresAndTemplates();

}
