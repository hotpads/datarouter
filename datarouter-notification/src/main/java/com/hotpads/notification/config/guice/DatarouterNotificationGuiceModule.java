package com.hotpads.notification.config.guice;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.handler.exception.NotificationServiceExceptionRecorder;
import com.hotpads.notification.NotificationApiClient;
import com.hotpads.notification.NotificationApiClient.NotificationApiClientHttpClientProvider;
import com.hotpads.util.http.client.HotPadsHttpClient;

public class DatarouterNotificationGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(ExceptionRecorder.class).to(NotificationServiceExceptionRecorder.class);
		bind(HotPadsHttpClient.class).annotatedWith(Names.named(NotificationApiClient.NOTIFICATION_API_CLIENT))
				.toProvider(NotificationApiClientHttpClientProvider.class).in(Singleton.class);
	}
}
