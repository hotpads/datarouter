package com.hotpads.notification.sender.template;

import java.util.List;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.sender.NotificationSender;

public interface NotificationTemplate<T extends NotificationSender> {

	void setRequests(List<NotificationRequest> requests);

}
