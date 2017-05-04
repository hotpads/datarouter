package com.hotpads.notification.sender.template;

import java.util.List;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;

public class NotificationTemplateRequest{
	private String templateId;
	private NotificationUserId userId;
	private NotificationDestination destination;
	private String notificationId;
	private List<NotificationRequest> requests;

	public NotificationTemplateRequest(String templateId, NotificationUserId userId,
			NotificationDestination destination, String notificationId, List<NotificationRequest> requests){
		this.templateId = templateId;
		this.userId = userId;
		this.destination = destination;
		this.notificationId = notificationId;
		this.requests = requests;
	}

	public String getTemplateId(){
		return templateId;
	}

	public NotificationUserId getUserId(){
		return userId;
	}

	public NotificationDestination getDestination(){
		return destination;
	}

	public String getNotificationId(){
		return notificationId;
	}

	public List<NotificationRequest> getRequests(){
		return requests;
	}
}