package com.hotpads.handler.exception;

import java.io.IOException;
import java.util.List;

import javax.inject.Singleton;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;

@Singleton
public class NotificationApiClient {

	private static final String NOTIFICATION_API_ENDPOINT = "https://localhost:8443/job/api/notification";

	private HotPadsHttpClient client;

	private boolean warmedup;

	public NotificationApiClient() {
		client = new HotPadsHttpClientBuilder().createInstance();
	}

	public void warmup() {
		if (!warmedup) {
			client.post(NOTIFICATION_API_ENDPOINT, new NotificationRequestDTO(), false);
			warmedup = true;
		}
	}

	public void call(List<NotificationRequest> requests) throws IOException {
		NotificationRequestDTO dto = new NotificationRequestDTO(requests);
		client.post(NOTIFICATION_API_ENDPOINT, dto, false);
	}

}
