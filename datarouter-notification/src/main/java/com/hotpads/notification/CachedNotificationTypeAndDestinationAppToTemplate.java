package com.hotpads.notification;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.databean.NotificationTypeAndDestinationAppToTemplate;
import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNotificationTypeAndDestinationAppToTemplate
extends Cached<Map<String, Map<NotificationDestinationApp, String>>>{

	private final NotificationNodes notificationNodes;

	@Inject
	public CachedNotificationTypeAndDestinationAppToTemplate(NotificationNodes notificationNodes){
		super(15, TimeUnit.SECONDS);
		this.notificationNodes = notificationNodes;
	}

	@Override
	protected Map<String, Map<NotificationDestinationApp, String>> reload(){
		Map<String, Map<NotificationDestinationApp, String>> result =
				notificationNodes.getNotificationTypeAndDestinationAppToTemplate().stream(null, null)
				.collect(Collectors.groupingBy(bean -> bean.getKey().getNotificationType(),
						Collectors.toMap(bean -> bean.getKey().getNotificationDestinationApp(),
								NotificationTypeAndDestinationAppToTemplate::getNotificationTemplate)));
		return result;//TODO remove
	}
}
