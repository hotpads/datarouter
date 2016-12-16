package com.hotpads.notification.timing;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.NotificationNodes;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNotificationTimingStrategy extends Cached<Map<String, NotificationTimingStrategy>>{

	private final NotificationNodes notificationNodes;

	@Inject
	public CachedNotificationTimingStrategy(NotificationNodes notificationNodes){
		super(15, TimeUnit.SECONDS);
		this.notificationNodes = notificationNodes;
	}

	@Override
	protected Map<String, NotificationTimingStrategy> reload(){
		return notificationNodes.getNotificationTimingStrategy()
				.stream(null, null)
				.collect(Collectors.toMap(timing -> timing.getKey().getName(), Function.identity()));
	}
}
