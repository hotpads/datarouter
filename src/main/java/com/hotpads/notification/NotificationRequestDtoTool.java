package com.hotpads.notification;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.notification.type.NotificationTypeFactory;
import com.hotpads.util.core.collections.Pair;

@Singleton
public class NotificationRequestDtoTool {

	@Inject
	private NotificationTypeFactory notificationTypeFactory;
	
	public List<NotificationRequest> toDatabeans(NotificationRequestDto[] dtos) throws IllegalArgumentException {
		List<NotificationRequest> notificationRequests = DrListTool.create();
		NotificationUserId userId;
		for (NotificationRequestDto request : dtos) {
			notificationTypeFactory.create(request.getType());
			userId = new NotificationUserId(NotificationUserType.valueOf(request.getUserType()), request.getUserId());
			notificationRequests.add(new NotificationRequest(userId, request.getType(), request.getData(), request
					.getChannel()));
		}
		return notificationRequests;
	}

	public List<NotificationRequestDto> toDtos(List<Pair<NotificationRequest, ExceptionRecord>> requests) {
		List<NotificationRequestDto> dtos = DrListTool.create();
		for (Pair<NotificationRequest, ExceptionRecord> request : requests) {
			dtos.add(new NotificationRequestDto(
					request.getLeft().getKey().getUserType().toString(),
					request.getLeft().getKey().getUserId(),
					request.getLeft().getType(),
					request.getLeft().getData(), 
					request.getLeft().getChannel()));
		}
		return dtos;
	}

}
