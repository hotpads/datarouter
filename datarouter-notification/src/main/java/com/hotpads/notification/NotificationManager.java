package com.hotpads.notification;

import java.util.Collection;
import java.util.List;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.result.NotificationSendingResult;

public interface NotificationManager{

	void request(NotificationRequest request);

	void request(Collection<NotificationRequest> requests);

	List<NotificationSendingResult> send(NotificationRequest notificationRequest);

	List<NotificationSendingResult> send(List<NotificationRequest> requests);

}
