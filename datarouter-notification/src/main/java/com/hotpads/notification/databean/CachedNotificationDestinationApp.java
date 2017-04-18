package com.hotpads.notification.databean;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.NotificationNodes;
import com.hotpads.notification.destination.NotificationDestinationAppName;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNotificationDestinationApp
extends Cached<Map<NotificationDestinationAppName, NotificationDestinationApp>>{
	private final NotificationNodes notificationNodes;

	@Inject
	public CachedNotificationDestinationApp(NotificationNodes notificationNodes){
		super(15, TimeUnit.SECONDS);
		this.notificationNodes = notificationNodes;
	}

	@Override
	protected Map<NotificationDestinationAppName, NotificationDestinationApp> reload(){
		return notificationNodes.getNotificationDestinationApp().stream(null, null)
				.collect(Collectors.toMap(app -> app.getKey().getName(), Function.identity()));
	}
}
