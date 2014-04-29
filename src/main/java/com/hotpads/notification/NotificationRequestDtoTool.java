package com.hotpads.notification;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.notification.type.NotificationTypeFactory;
import com.hotpads.util.core.ListTool;

@Singleton
public class NotificationRequestDtoTool {

	@Inject
	private NotificationTypeFactory notificationTypeFactory;
	
	public List<NotificationRequest> toDatabeans(NotificationRequestDto[] dtos) throws IllegalArgumentException {
		List<NotificationRequest> notificationRequests = ListTool.create();
		NotificationUserId userId;
		for (NotificationRequestDto request : dtos) {
			notificationTypeFactory.create(request.getType());
			userId = new NotificationUserId(NotificationUserType.valueOf(request.getUserType()), request.getUserId());
			notificationRequests.add(new NotificationRequest(userId, request.getType(), request.getData(), request
					.getChannel()));
		}
		return notificationRequests;
	}

	public List<NotificationRequestDto> toDtos(List<NotificationRequest> requests) {
		List<NotificationRequestDto> dtos = ListTool.create();
		for (NotificationRequest request : requests) {
			dtos.add(new NotificationRequestDto(
					request.getKey().getUserType().toString(),
					request.getKey().getUserId(),
					request.getType(),
					request.getData(), 
					request.getChannel()));
		}
		return dtos;
	}

}
