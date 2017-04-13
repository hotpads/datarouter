package com.hotpads.notification.databean;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.NotificationNodes;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNotificationTypeConfig
extends Cached<Map<String, NotificationTypeConfig>>{
	private final NotificationNodes notificationNodes;

	@Inject
	public CachedNotificationTypeConfig(NotificationNodes notificationNodes){
		super(15, TimeUnit.SECONDS);
		this.notificationNodes = notificationNodes;
	}

	@Override
	protected Map<String, NotificationTypeConfig> reload(){
		return notificationNodes.getNotificationTypeConfig().stream(null, null)
				.collect(Collectors.toMap(config -> config.getKey().getName(), Function.identity()));
	}
}
