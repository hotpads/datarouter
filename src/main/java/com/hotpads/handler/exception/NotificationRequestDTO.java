package com.hotpads.handler.exception;

import java.util.List;

import javax.inject.Inject;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.notification.type.NotificationTypeFactory;
import com.hotpads.util.core.ListTool;

public class NotificationRequestDTO {

	private List<Shit> notificationRequests;

	private class Shit {

		private String userType;
		private String userId;
		private String type;
		private String data;
		private String channel;

		public Shit(String userType, String userId, String type, String data, String channel) {
			this.userType = userType;
			this.userId = userId;
			this.type = type;
			this.data = data;
			this.channel = channel;
		}
	}
	
	public NotificationRequestDTO() {
		notificationRequests = ListTool.create();
	}
	
	public NotificationRequestDTO(List<NotificationRequest> requests) {
		this();
		addAll(requests);
	}

	public void add(NotificationRequest request) {
		notificationRequests.add(new Shit(
				request.getKey().getUserType().toString(),
				request.getKey().getUserId(),
				request.getType(),
				request.getData(), 
				request.getChannel()));
	}

	public void addAll(Iterable<NotificationRequest> requests) {
		for (NotificationRequest request : requests) {
			add(request);
		}
	}

	public List<NotificationRequest> checkTypeAndGetAll(NotificationTypeFactory notificationTypeFactory) {
		List<NotificationRequest> requests = ListTool.create();
		for (Shit request : this.notificationRequests) {
			notificationTypeFactory.create(request.type);
			requests.add(
					new NotificationRequest(
							new NotificationUserId(
									NotificationUserType.valueOf(request.userType),
									request.userId),
							request.type,
							request.data,
							request.channel));
		}
		return requests;
	}

}
