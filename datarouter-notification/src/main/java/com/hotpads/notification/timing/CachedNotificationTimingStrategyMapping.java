package com.hotpads.notification.timing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.NotificationNodes;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNotificationTimingStrategyMapping extends
		Cached<Map<String,List<NotificationTimingStrategyMapping>>>{

	private final NotificationNodes notificationNodes;

	@Inject
	public CachedNotificationTimingStrategyMapping(NotificationNodes notificationNodes){
		super(15, TimeUnit.SECONDS);
		this.notificationNodes = notificationNodes;
	}

	@Override
	protected Map<String,List<NotificationTimingStrategyMapping>> reload(){
		return notificationNodes.getNotificationTimingStrategyMapping()
				.stream(null, null)
				.collect(Collectors.toMap(
						mapping -> mapping.getKey().getType(),
						mapping -> Arrays.asList(mapping),
						(left, right) -> {
							List<NotificationTimingStrategyMapping> result = new ArrayList<>(left);
							result.addAll(right);
							return result;
						}));
	}
}
