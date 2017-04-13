package com.hotpads.notification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.databean.CachedNotificationDestinationApp;
import com.hotpads.notification.databean.CachedNotificationTypeConfig;
import com.hotpads.notification.databean.NotificationDestinationApp;
import com.hotpads.notification.databean.NotificationDestinationAppKey;
import com.hotpads.notification.databean.NotificationTypeConfig;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationAppName;
import com.hotpads.notification.preference.NotificationDestinationAppGroupName;
import com.hotpads.notification.preference.NotificationPreference;
import com.hotpads.notification.preference.NotificationPreferenceKey;
import com.hotpads.notification.preference.NotificationTypeGroupName;
import com.hotpads.notification.sender.template.CachedNotificationTemplate;
import com.hotpads.notification.timing.CachedNotificationTimingStrategy;
import com.hotpads.notification.timing.CachedNotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.timing.NotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategyMappingKey;

@Singleton
public class NotificationDao{
	@Inject
	private NotificationNodes notificationNodes;
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
	@Inject
	private CachedNotificationTemplate notificationTemplates;

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

	//TODO test
	public Set<NotificationDestination> filterOutOptedOut(NotificationTypeGroupName typeGroup,
			Set<NotificationDestination> destinations, Set<NotificationDestinationApp> destinationApps){
		if(typeGroup == null || destinations.isEmpty() || destinationApps.isEmpty()){
			return destinations;
		}

		String userToken = destinations.iterator().next().getKey().getToken();
		Set<NotificationDestinationAppName> optedOutApps = getOptedOutApps(typeGroup, userToken, destinationApps);

		return destinations.stream()
				.filter(destination -> !optedOutApps.contains(destination.getKey().getApp()))
				.collect(Collectors.toSet());
	}

	//TODO test
	public Map<NotificationDestination,String> buildTemplateClassMap(Set<NotificationDestination> destinations,
			Map<NotificationDestinationAppName,String> appToTemplateMap){
		return destinations.stream()
				.collect(Collectors.toMap(Function.identity(), destination -> notificationTemplates.get().get(
						appToTemplateMap.get(destination.getKey().getApp()))));
	}

	//TODO test
	private Set<NotificationDestinationAppName> getOptedOutApps(NotificationTypeGroupName typeGroup, String userToken,
			Collection<NotificationDestinationApp> destinationApps){
		Set<NotificationPreferenceKey> preferenceKeys = destinationApps.stream()
				.map(NotificationDestinationApp::getGroupName)
				.map(appGroup -> new NotificationPreferenceKey(userToken, appGroup, typeGroup))
				.collect(Collectors.toSet());
		if(preferenceKeys.size() == 0){
			return new HashSet<>();
		}

		Set<NotificationDestinationAppGroupName> optedOutGroupNames = notificationNodes.getNotificationPreference()
				.getMulti(preferenceKeys, null)
				.stream()
				.map(NotificationPreference::getKey)
				.map(NotificationPreferenceKey::getDeviceGroup)
				.collect(Collectors.toSet());

		return destinationApps.stream()
				.filter(app -> optedOutGroupNames.contains(app.getGroupName()))
				.map(NotificationDestinationApp::getKey)
				.map(NotificationDestinationAppKey::getName)
				.collect(Collectors.toSet());
	}
}
