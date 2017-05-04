package com.hotpads.notification.sender.template;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.result.NotificationFailureReason;

public class UnbuiltTemplate{
	private NotificationRequest request;
	private NotificationFailureReason failureReason;
	private String detailedFailureReason;//TODO do codes/enums exist for templates?

	public UnbuiltTemplate(NotificationRequest request, NotificationFailureReason failureReason,
			String detailedFailureReason){
		this.request = request;
		this.failureReason = failureReason;
		this.detailedFailureReason = detailedFailureReason;
	}
}