package com.hotpads.notification;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hp.gagawa.java.elements.Object;

@Singleton
public class NotificationApiClient {

	private static final String NOTIFICATION_API_ENDPOINT = "https://localhost:8443/job/api/notification";

	private HotPadsHttpClient client;

	@Inject
	private NotificationRequestDtoTool dtoTool;

	private boolean warmedup;

	@Inject
	public NotificationApiClient(ExceptionHandlingConfig exceptionHandlingConfig) {
		client = new HotPadsHttpClientBuilder().createInstance();
	}

	public void warmup() {
		if (!warmedup) {
			client.post(NOTIFICATION_API_ENDPOINT, new Object[]{}, false);
			warmedup = true;
		}
	}

	public void call(List<NotificationRequest> requests) throws IOException {
		if (dtoTool == null) { //if no guice
			dtoTool = new NotificationRequestDtoTool();
		}
		client.post(NOTIFICATION_API_ENDPOINT, dtoTool.toDtos(requests), false);
	}

}
