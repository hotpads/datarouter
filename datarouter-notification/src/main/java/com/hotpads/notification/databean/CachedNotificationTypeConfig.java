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
//		return notificationNodes.getNotificationTimingStrategyMapping()
//				.stream(null, null)
//				.collect(Collectors.toMap(
//						mapping -> mapping.getKey().getType(),
//						mapping -> Arrays.asList(mapping),
//						(left, right) -> {
//							List<NotificationTimingStrategyMapping> result = new ArrayList<>(left);
//							result.addAll(right);
//							return result;
//						}));
		return notificationNodes.getNotificationTypeConfig().stream(null, null)
				.collect(Collectors.toMap(config -> config.getKey().getType(), Function.identity()));
	}
}
