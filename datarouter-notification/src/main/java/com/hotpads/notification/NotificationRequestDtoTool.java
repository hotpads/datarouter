package com.hotpads.notification;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

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

	public List<NotificationRequest> toDatabeans(NotificationRequestDto[] dtos){
		List<NotificationRequest> notificationRequests = new ArrayList<>();
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
		List<NotificationRequestDto> dtos = new ArrayList<>();
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

	public static List<NotificationRequestDto> convert(List<NotificationRequest> requests) {
		List<NotificationRequestDto> dtos = new ArrayList<>(requests.size());
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