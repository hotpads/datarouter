package com.hotpads.notification;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;

@Singleton
public class NotificationApiClient {

	private HotPadsHttpClient client;
	private NotificationRequestDtoTool dtoTool;
	private ExceptionHandlingConfig exceptionHandlingConfig;

	@Inject
	public NotificationApiClient(NotificationRequestDtoTool dtoTool, ExceptionHandlingConfig exceptionHandlingConfig) {
		this.client = new HotPadsHttpClientBuilder().createInstance();
		this.exceptionHandlingConfig = exceptionHandlingConfig;
		this.dtoTool = dtoTool;
	}

	public void call(List<NotificationRequest> requests) throws IOException {
		client.post(exceptionHandlingConfig.getNotificationApiEndPoint(), dtoTool.toDtos(requests), false);
	}

}
