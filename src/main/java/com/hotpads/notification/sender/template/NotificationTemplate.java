package com.hotpads.notification.sender.template;

import java.util.List;

import com.hotpads.notification.databean.NotificationRequest;

public interface NotificationTemplate<T> {

	void setRequests(List<NotificationRequest> requests);

}
