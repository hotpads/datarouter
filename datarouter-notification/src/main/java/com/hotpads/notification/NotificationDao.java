package com.hotpads.notification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.notification.databean.BaseStringWrapperField;
import com.hotpads.notification.databean.CachedNotificationDestinationApp;
import com.hotpads.notification.databean.CachedNotificationTypeConfig;
import com.hotpads.notification.databean.NotificationDestinationApp;
import com.hotpads.notification.databean.NotificationDestinationAppKey;
import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationTypeConfig;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationAppName;
import com.hotpads.notification.preference.NotificationDestinationAppGroupName;
import com.hotpads.notification.preference.NotificationPreferenceKey;
import com.hotpads.notification.preference.NotificationTypeGroupName;
import com.hotpads.notification.sender.template.CachedNotificationTemplate;
import com.hotpads.notification.timing.CachedNotificationTimingStrategy;
import com.hotpads.notification.timing.CachedNotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.timing.NotificationTimingStrategyMapping;
import com.hotpads.notification.timing.NotificationTimingStrategyMappingKey;
import com.hotpads.util.core.iterable.BatchingIterable;

@Singleton
public class NotificationDao{
	/**
	 * @deprecated shrink the number of notificationIds stored while still on mysql
	 */
	@Deprecated
	private static final int MAX_NOTIFICATION_IDS_STORABLE = 6;

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
	@Inject
	private Gson gson;

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

	public NotificationTypeConfig getNotificationTypeConfig(String type){
		return typeConfigs.get().get(type);
	}

	public Set<NotificationDestinationApp> getNotificationDestinationAppsByName(
			Collection<NotificationDestinationAppName> names){
		Map<NotificationDestinationAppName, NotificationDestinationApp> map = destinationApps.get();
		return names.stream()
				.map(map::get)
				.collect(Collectors.toSet());
	}

	public Set<NotificationDestination> filterOutOptedOut(NotificationTypeGroupName typeGroup,
			Set<NotificationDestination> destinations, Set<NotificationDestinationApp> destinationApps){
		if(typeGroup == null || typeGroup.persistentString == null || destinations.isEmpty() || destinationApps
				.isEmpty()){
			return destinations;
		}

		String userToken = destinations.iterator().next().getKey().getToken();
		if(userToken == null){
			return destinations;
		}
		Set<NotificationDestinationAppName> optedOutApps = getOptedOutApps(typeGroup, userToken, destinationApps);

		return destinations.stream()
				.filter(destination -> !optedOutApps.contains(destination.getKey().getApp()))
				.collect(Collectors.toSet());
	}

	private Set<NotificationDestinationAppName> getOptedOutApps(NotificationTypeGroupName typeGroup, String userToken,
			Collection<NotificationDestinationApp> destinationApps){

		Set<NotificationPreferenceKey> preferenceKeys = destinationApps.stream()
				.map(NotificationDestinationApp::getGroupName)
				.filter(BaseStringWrapperField::nonNull)
				.map(appGroup -> new NotificationPreferenceKey(userToken, appGroup, typeGroup))
				.collect(Collectors.toSet());
		if(preferenceKeys.size() == 0){
			return new HashSet<>();
		}

		Set<NotificationDestinationAppGroupName> optedOutGroupNames = notificationNodes.getNotificationPreference()
				.getKeys(preferenceKeys, null)
				.stream()
				.map(NotificationPreferenceKey::getDeviceGroup)
				.collect(Collectors.toSet());

		return destinationApps.stream()
				.filter(app -> optedOutGroupNames.contains(app.getGroupName()))
				.map(NotificationDestinationApp::getKey)
				.map(NotificationDestinationAppKey::getName)
				.collect(Collectors.toSet());
	}

	//TODO test
	public Map<NotificationDestination,String> buildTemplateClassMap(Set<NotificationDestination> destinations,
			Map<NotificationDestinationAppName,String> appToTemplateMap){
		return destinations.stream()
				.collect(Collectors.toMap(Function.identity(), destination -> notificationTemplates.get().get(
						appToTemplateMap.get(destination.getKey().getApp()))));
	}

	public void logItems(Collection<NotificationRequest> requests, List<String> notificationIds){
		String idsString = gson.toJson(DrListTool.getFirstNElements(notificationIds, MAX_NOTIFICATION_IDS_STORABLE));
		for(List<NotificationRequest> requestBatch : new BatchingIterable<>(requests, 100)){
			List<NotificationItemLog> itemLogs = requestBatch.stream()
					.map(request -> new NotificationItemLog(request, idsString))
					.collect(Collectors.toList());
			notificationNodes.getNotificationItemLog().putMulti(itemLogs, null);
		}
	}
}
