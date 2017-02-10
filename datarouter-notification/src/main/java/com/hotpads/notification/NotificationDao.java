package com.hotpads.notification;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.notification.timing.CachedNotificationTimingStrategy;
import com.hotpads.notification.timing.CachedNotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.timing.NotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategyMappingKey;
import com.hotpads.notification.type.NotificationType;

@Singleton
public class NotificationDao{
	@Inject
	private CachedNotificationTimingStrategyMapping notificationTimingMappings;
	@Inject
	private CachedNotificationTimingStrategy notificationTimings;
	@Inject
	private CachedNotificationTypeAndDestinationAppToTemplate typeAndDestinationToTemplateMap;

	public NotificationTimingStrategy getTiming(NotificationTimingStrategyMappingKey key){
		//get every mapping that matches the type
		return notificationTimingMappings.get().getOrDefault(key.getType(), Collections.emptyList()).stream()
				//filter out every mapping channelPrefix that is not a prefix of key's channel
				.filter(potentialMatch -> key.getChannelPrefix().startsWith(potentialMatch.getKey().getChannelPrefix()))
				//find the maximum length prefix that remains, and return its associated timing (or null)
				.max((left, right) -> Integer.compare(left.getKey().getChannelPrefix().length(),
						right.getKey().getChannelPrefix().length()))
				.map(NotificationTimingStrategyMapping::getTimingStrategy)
				.map(timingName -> notificationTimings.get().get(timingName))
				.orElse(null);
	}

	public Map<NotificationDestinationApp, String> getDestinationAppToTemplateMappingForType(NotificationType type){
		return typeAndDestinationToTemplateMap.get().getOrDefault(type.getClass().getSimpleName(), Collections
				.emptyMap());
	}
}
