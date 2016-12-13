package com.hotpads.notification.timing;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.notification.NotificationNodes;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNotificationTimingStrategy extends Cached<Map<String, NotificationTimingStrategy>>{

	private final NotificationNodes notificationNodes;

	@Inject
	public CachedNotificationTimingStrategy(NotificationNodes notificationNodes){
		super(15, TimeUnit.SECONDS);//TODO change later?
		this.notificationNodes = notificationNodes;
	}

	@Override
	protected Map<String, NotificationTimingStrategy> reload(){
		Map<String, NotificationTimingStrategy> timings = notificationNodes
				.getNotificationTimingStrategy()
				.stream(null, null)
				.collect(Collectors.toMap(timing -> timing.getKey().getName(),timing -> timing));
		return timings;//TODO just for debugging. remove later
	}
}
