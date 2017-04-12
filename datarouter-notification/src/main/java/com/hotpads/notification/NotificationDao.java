package com.hotpads.notification;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.databean.CachedNotificationDestinationApp;
import com.hotpads.notification.databean.CachedNotificationTypeConfig;
import com.hotpads.notification.databean.NotificationDestinationApp;
import com.hotpads.notification.databean.NotificationTypeConfig;
import com.hotpads.notification.destination.NotificationDestinationAppName;
import com.hotpads.notification.timing.CachedNotificationTimingStrategy;
import com.hotpads.notification.timing.CachedNotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.timing.NotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategyMappingKey;

@Singleton
public class NotificationDao{
	@Inject
	private CachedNotificationTimingStrategyMapping timingMappings;
	@Inject
	private CachedNotificationTimingStrategy timings;
	@Inject
	private CachedNotificationTypeAndDestinationAppToTemplate typeAndDestinationToTemplateMap;
	@Inject
	private CachedNotificationTypeConfig typeConfigs;
	@Inject
	private CachedNotificationDestinationApp destinationApps;

	public NotificationTimingStrategy getTiming(NotificationTimingStrategyMappingKey key){
		//get every mapping that matches the type
		return timingMappings.get().getOrDefault(key.getType(), Collections.emptyList()).stream()
				//filter out every mapping channelPrefix that is not a prefix of key's channel
				.filter(potentialMatch -> key.getChannelPrefix().startsWith(potentialMatch.getKey().getChannelPrefix()))
				//find the maximum length prefix that remains, and return its associated timing (or null)
				.max((left, right) -> Integer.compare(left.getKey().getChannelPrefix().length(),
						right.getKey().getChannelPrefix().length()))
				.map(NotificationTimingStrategyMapping::getTimingStrategy)
				.map(timings.get()::get)
				.orElseThrow(() -> new RuntimeException("Failed to find NotificationTimingStrategy from key: " + key));
	}

	public Map<NotificationDestinationAppName,String> getDestinationAppToTemplateMappingForType(
			String notificationType){
		return typeAndDestinationToTemplateMap.get().getOrDefault(notificationType, Collections.emptyMap());
	}

	public NotificationTypeConfig getNotificationTypeConfig(String type){//TODO maybe just inline...
		return typeConfigs.get().get(type);
	}

	public Set<NotificationDestinationApp> getNotificationDestinationAppsByName(
			Collection<NotificationDestinationAppName> names){
		Map<NotificationDestinationAppName, NotificationDestinationApp> map = destinationApps.get();
		return names.stream()
				.map(map::get)
				.collect(Collectors.toSet());
	}


}
