package com.hotpads.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationLogKey;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationRequestKey;
import com.hotpads.notification.databean.NotificationTypeConfig;
import com.hotpads.notification.databean.NotificationTypeConfigKey;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.notification.result.NotificationFailureReason;
import com.hotpads.notification.result.NotificationSendingResult;
import com.hotpads.notification.sender.NotificationSender;
import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.sender.template.NotificationTemplateFactory;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.timing.NotificationTimingStrategyMappingKey;
import com.hotpads.notification.tracking.NotificationTrackingService;
import com.hotpads.notification.type.NotificationTypeFactory;
import com.hotpads.util.core.collections.Range;

@Singleton
public class NotificationService{
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	private static final String HBASE1_NAME = new ClientId("hbase1", true).getName();
	/**
	 * @deprecated shrink the number of notificationIds stored while still on mysql
	 */
	@Deprecated
	private static final int MAX_NOTIFICATION_IDS_STORABLE = 6;

	@Inject
	private DatarouterInjector injector;
	@Inject
	private NotificationTemplateFactory templateFactory;
	@Inject
	private NotificationTypeFactory typeFactory;
	@Inject
	private NotificationTrackingService notificationTrackingService;
	@Inject
	private ExceptionRecorder exceptionRecorder;
	@Inject
	NotificationServiceCallbacks callbacks;
	@Inject
	private NotificationNodes notificationNodes;//TODO this will be moved into own webapp
	@Inject
	private Gson gson;
	@Inject
	private ClientAvailabilitySettings availabilitySettings;
	@Inject
	private NotificationDao notificationDao;

	public List<NotificationSendingResult> sendNotifications(List<NotificationRequest> requests, String jobName){
		NotificationRequest firstRequest = requests.get(0);
		NotificationUserId userId = firstRequest.getKey().getNotificationUserId();
		NotificationUserType userType = userId.getType();

		List<NotificationSendingResult> results = new ArrayList<>();
		List<NotificationDestination> destinations;

		//TODO there was no try/catch about typeFactory here. why not?
		//TODO convert all tables to use one of these two (type or name)? could easily confuse someone currently
		String notificationType = firstRequest.getType();//fully qualified name (package + class)
		//TODO try catch for more useful message?
		NotificationTypeConfig typeConfig = notificationNodes.getNotificationTypeConfig().get(//TODO cache later
				new NotificationTypeConfigKey(notificationType), null);
		String simpleName = typeConfig.getName();

		Map<NotificationDestinationApp,String> appToTemplateMap = notificationDao
				.getDestinationAppToTemplateMappingForType(simpleName);

		if(typeConfig.getNeedsRemoveDisabledCallback()){
			callbacks.removeDisabledSearchDestinationApps(firstRequest.getType(), appToTemplateMap);
		}

		if(userType.needNotificationDestinationService()){
			destinations = callbacks.getActiveDestinations(userId, appToTemplateMap.keySet());
		}else{
			destinations = callbacks.getDestinations(appToTemplateMap.keySet(), userId);
		}

		//TODO FIRST bring filter method into dr-noti

		Map<NotificationDestination,String> tempByDest = callbacks.filterOutOptedOut(notificationType, destinations,
				appToTemplateMap);

		//TODO FIRST ^

		if(tempByDest.isEmpty()){
			remove(requests);
			return results;
		}

		List<NotificationRequest> selectedRequests = typeConfig.getNeedsFilterOutIrrelevantCallback()
				? callbacks.filterOutIrrelevantNotificationRequests(notificationType, requests) : requests;
		if(selectedRequests.isEmpty()){
			remove(requests);
			return results;
		}

		boolean sent = false;
		List<String> sentNotificationIds = new ArrayList<>();
		for(Entry<NotificationDestination,String> entry : tempByDest.entrySet()){
			NotificationDestination notificationDestination = entry.getKey();
			String templateClass = entry.getValue();
			NotificationSendingResult result = new NotificationSendingResult(userId, templateClass);
			results.add(result);
			String uuid = notificationTrackingService.generateId();
			NotificationSender sender;
			NotificationTemplate template;
			try{
				template = templateFactory.create(templateClass);
				sender = injector.getInstance(template.getNotificationSender());
			}catch(RuntimeException e){
				logger.error("Error creating notification sender and template", e);
				result.setFail(NotificationFailureReason.FAILED_TO_GET_NEEDED_INSTANCES);
				if(jobName != null){
					exceptionRecorder.tryRecordException(e, jobName);
				}else{
					throw e;
				}
				continue;
			}
			sender.setUserId(userId, notificationDestination);
			try{
				sender.setTemplate(template);
			}catch(IllegalArgumentException e){
				logger.warn(userId + " does not fit the requirements for "
						+ template.getClass().getSimpleName() + ": " + e.getMessage());
				NotificationCounters.inc("wrong UserId");
				result.setFail(NotificationFailureReason.WRONG_USER_ID);
				remove(requests);//TODO improve multiple device
				continue;
			}
			template.setRequests(selectedRequests);
			template.setNotificationId(uuid);

			String appName = notificationDestination.getKey().getApp().persistentString;
			NotificationCounters.inc("send attempt");
			NotificationCounters.inc("send attempt " + simpleName);
			NotificationCounters.inc("send attempt " + simpleName + " " + appName);
			NotificationCounters.inc("send attempt " + sender.getClass().getSimpleName());
			NotificationCounters.inc("send attempt " + sender.getClass().getSimpleName() + " " + appName);
			NotificationCounters.inc("send attempt " + template.getClass().getSimpleName());
			NotificationCounters.inc("send attempt " + template.getClass().getSimpleName() + " " + appName);
			try{
				if(sender.send(result)){
					NotificationCounters.inc("send success");
					NotificationCounters.inc("send success " + simpleName);
					NotificationCounters.inc("send success " + simpleName + " "
							+ appName);
					NotificationCounters.inc("send success " + sender.getClass().getSimpleName());
					NotificationCounters.inc("send success " + sender.getClass().getSimpleName() + " " + appName);
					NotificationCounters.inc("send success " + template.getClass().getSimpleName());
					NotificationCounters.inc("send success " + template.getClass().getSimpleName() + " " + appName);
					sent = true;
					sentNotificationIds.add(uuid);
					log(selectedRequests, template.getClass(), uuid, notificationDestination.getKey().getDeviceId());
				}else{
					result.setFailIfnotSet(NotificationFailureReason.DISCARD_BY_SENDER);
					NotificationCounters.inc("discard");
					logger.info("{} discared by the sender", simpleName);
					remove(requests);//TODO improve multiple device
				}
			}catch(Exception e){
				result.setFail(NotificationFailureReason.SENDER_FAILED);
				NotificationCounters.inc("send failed");
				NotificationCounters.inc("send failed " + simpleName);
				NotificationCounters.inc("send failed " + simpleName + " " + appName);
				NotificationCounters.inc("send failed " + sender.getClass().getSimpleName());
				NotificationCounters.inc("send failed " + sender.getClass().getSimpleName() + " " + appName);
				NotificationCounters.inc("send failed " + template.getClass().getSimpleName());
				NotificationCounters.inc("send failed " + template.getClass().getSimpleName() + " " + appName);
				if(jobName != null){
					exceptionRecorder.tryRecordException(e, jobName);
				}else{
					throw e;
				}
				logger.error("Error sending notification for request(s) " + requests, e);
			}
		}
		if(sent){//TODO improve multiple device
			if(typeConfig.getNeedsOnSuccessCallback()){
				callbacks.onSuccess(notificationType, selectedRequests);
			}
			if(availabilitySettings.getAvailabilityForClientName(HBASE1_NAME).write.getValue() || jobName != null){
				logItems(selectedRequests, sentNotificationIds);
				remove(requests);
			}
		}
		return results;
	}

	private void logItems(Collection<NotificationRequest> requests, List<String> notificationIds){
		String idsString = gson.toJson(DrListTool.getFirstNElements(notificationIds, MAX_NOTIFICATION_IDS_STORABLE));
		List<NotificationItemLog> itemLogs = requests.stream()
				.map(request -> new NotificationItemLog(request, idsString))
				.collect(Collectors.toList());

		notificationNodes.getNotificationItemLog().putMulti(itemLogs, null);
	}

	private void log(List<NotificationRequest> requests, Class<? extends NotificationTemplate> template, String uuid,
			String deviceId){
		List<String> itemIds = new ArrayList<>();

		for(NotificationRequest request : requests){
			itemIds.add(request.getData());
		}
		NotificationRequest firstRequest = requests.get(0);
		NotificationLog notificationLog = new NotificationLog(firstRequest.getKey().getNotificationUserId(), new Date(),
				template.getName(), firstRequest.getType(), itemIds, firstRequest.getChannel(), uuid, deviceId);
		notificationNodes.getNotificationLog().put(notificationLog, null);
	}

	private void remove(List<NotificationRequest> requests){
		notificationNodes.getNotificationRequest().deleteMulti(DatabeanTool.getKeys(requests), null);
	}

	public int processUser(List<NotificationRequest> userRequests, String jobName){
		List<List<NotificationRequest>> groups = new ArrayList<>();
		Map<NotificationTimingStrategyMappingKey, NotificationTimingStrategy> timingCache = new HashMap<>();
		for(NotificationRequest request : userRequests){
			processUserRequest(request, groups, timingCache);
		}
		int notificationSent = 0;
		Set<NotificationRequestKey> toDelete = new HashSet<>();
		for(List<NotificationRequest> group : groups){
			if(shouldBeSent(group, timingCache)){
				notificationSent += NotificationSendingResult.countSuccesses(sendNotifications(group, jobName));
			}else{
				for(NotificationRequest request : group){
					if(canBeDropped(request, timingCache)){
						toDelete.add(request.getKey());
						logger.info("notification " + request + " dropped");
						NotificationCounters.inc("dropped");
					}
				}
			}
		}
		notificationNodes.getNotificationRequest().deleteMulti(toDelete, null);
		return notificationSent;
	}

	private void processUserRequest(NotificationRequest userRequest, List<List<NotificationRequest>> groupedRequests,
			Map<NotificationTimingStrategyMappingKey, NotificationTimingStrategy> timingCache){
		try{
			typeFactory.create(userRequest.getType());
			//TODO figure out how to recreate the overall behavior of this try/catch, which potentially prevents worse
			//errors from happening later (that might affect more than one request)
			//original behavior would have skipped this specific request but continued on with the process. how to make
			//sure that the type is valid? just check DB to make sure it's been registered as a type? let the client
			//worry about their registered types actually existing in the callbacks.
		}catch(Exception e){
			logger.warn("Error creating " + userRequest.getType() + ", notification request " + userRequest
					+ " not handled", e);
			return;
		}
		NotificationTimingStrategy timing = timingCache.computeIfAbsent(
				new NotificationTimingStrategyMappingKey(userRequest), key -> notificationDao.getTiming(key));
		if(tooYoungtoBeProcessed(userRequest, timing)){
			return;
		}
		for(List<NotificationRequest> group : groupedRequests){
			if(!canBeGrouped(group, userRequest, timing)){
				continue;
			}
			group.add(userRequest);
			return;
		}
		groupedRequests.add(DrListTool.wrap(userRequest));
	}

	private boolean canBeDropped(NotificationRequest request, Map<NotificationTimingStrategyMappingKey,
			NotificationTimingStrategy> timingCache){
		long ageMs = System.currentTimeMillis() - request.getKey().getSentAtMs();
		return ageMs >= timingCache.get(new NotificationTimingStrategyMappingKey(request)).getDroppableAgeMs();
	}

	private boolean tooYoungtoBeProcessed(NotificationRequest request, NotificationTimingStrategy timing){
		long ageMs = System.currentTimeMillis() - request.getKey().getSentAtMs();
		return ageMs <= timing.getMinSendableAgeMs();
	}

	private boolean shouldBeSent(List<NotificationRequest> group, Map<NotificationTimingStrategyMappingKey,
			NotificationTimingStrategy> timingCache){
		NotificationRequest lastRequest = DrCollectionTool.getLast(group);
		NotificationRequest firstRequest = DrCollectionTool.getFirst(group);
		NotificationTimingStrategy timing = timingCache.get(new NotificationTimingStrategyMappingKey(lastRequest));
		String channel = firstRequest.getChannel();
		Date now = new Date();
		boolean reachedMaxSize = Objects.equals(group.size(), timing.getMaxItems());
		boolean noNewRequest = now.getTime() - lastRequest.getKey().getSentAtMs() >= timing.getMinDelayMs();
		boolean oldestIsTooOld = now.getTime() - firstRequest.getKey().getSentAtMs() >= timing.getMaxDelayMs();
		if(reachedMaxSize || noNewRequest || oldestIsTooOld){
			//getLastNotification of that type
			NotificationUserId userId = lastRequest.getKey().getNotificationUserId();
			NotificationLogKey userPrefix = new NotificationLogKey(userId);
			Range<NotificationLogKey> range = new Range<>(userPrefix, true, userPrefix, true);
			//Mainly user have not a lot of notification and the yougest is at the beginning
			Iterable<NotificationLog> logs = notificationNodes.getNotificationLog().scan(range,
					new Config().setIterateBatchSize(100));
			//filter by type
			NotificationLog lastLogOfThatType = null;
			NotificationLog lastLogOfThatTypeAndThatChannel = null;
			for(NotificationLog log : logs){
				if(log.getType().equals(lastRequest.getType())){
					if(lastLogOfThatType == null){
						lastLogOfThatType = log;
					}
					if(channel != null && channel.equals(log.getChannel())){
						lastLogOfThatTypeAndThatChannel = log;
					}
				}
				if(lastLogOfThatType != null && (channel == null || lastLogOfThatTypeAndThatChannel != null)){
					break;
				}
			}
			if(lastLogOfThatType == null){
				return true;
			}
			boolean enoughTimeElapsedForTheType = hasEnoughTimeElapsed(now, lastLogOfThatType,
					timing.getStandardDelayMs());
			if(channel == null || lastLogOfThatTypeAndThatChannel == null){
				return enoughTimeElapsedForTheType;
			}
			return enoughTimeElapsedForTheType && hasEnoughTimeElapsed(now, lastLogOfThatTypeAndThatChannel,
					timing.getDelayForChannelMs());
		}
		return false;
	}

	private boolean hasEnoughTimeElapsed(Date now, NotificationLog log, Long delayMs){
		return now.getTime() - log.getKey().getCreated().getTime() >= delayMs;
	}

	private boolean canBeGrouped(List<NotificationRequest> group, NotificationRequest request,
			NotificationTimingStrategy timing){
		NotificationRequest first = group.get(0);
		String grouptype = first.getType();//TODO replace with string comp
		return grouptype.equals(request.getType()) && group.size() < timing.getMaxItems() && haveSameChannel(first,
				request);
	}

	private static boolean haveSameChannel(NotificationRequest first, NotificationRequest request){
		return first.getChannel() == null && request.getChannel() == null
				|| first.getChannel() != null && first.getChannel().equals(request.getChannel());
	}

	//TODO below here maybe in own service or a dao
	//TODO WIP recreating NotificationPreferenceService method from NotificationServiceCallbacks without using
	//HP enums
//	public Map<NotificationDestination,String> filterOutOptedOut(NotificationTypeConfig typeConfig,
//			List<NotificationDestination> destinations, Map<NotificationDestinationApp,String> appToTemplateMap){
//		if(destinations.isEmpty()){
//			return new HashMap<>();
//		}
//
//		Set<NotificationDestinationApp> optedOutApps = getOptedOutApps(typeConfig, destinations.get(0).getKey()
//				.getToken(), appToTemplateMap.keySet());
//
//		Map<NotificationDestination,String> matchingTemplates = new HashMap<>();
//		for(NotificationDestination destination : destinations){
//			//TODO this get is unnecessary except to detect and ignore missing templates
//			String templateClass = notificationTemplates.get().get(appToTemplateMap.get(destination.getKey().getApp()));
//			if(templateClass != null && !optedOutApps.contains(destination.getKey().getApp())){
//				matchingTemplates.put(destination, templateClass);
//			}
//		}
//		return matchingTemplates;
//	}

//	private Set<NotificationDestinationApp> getOptedOutApps(NotificationTypeConfig typeConfig, String userToken,
//			Set<NotificationDestinationApp> apps){
//		//(OLD) get NotificationTypeGroup that matches NotificationType (using HotpadsNotificationTypeGroup)
////		NotificationTypeGroup typeGroup = HotpadsNotificationTypeGroup.getForType(notificationType
////				.getClass()).map(hpTypeGroup -> hpTypeGroup.typeGroup).orElse(null);
//		//(NEW) get notificationTypeGroup
//		//TODO multiple groups in the future?
//		String typeGroup = typeConfig.getGroup();//TODO should this be NotificationTypeGroup?
//		if(typeGroup == null){
//			return new HashSet<>();
//		}
//
//		//generate NotificationPreferenceKeys for each NotificationDestinationApp, such that a
//		//HotpadsNotificationDeviceGroup exists that contains the NotificationDestinationApp
//		Set<NotificationPreferenceKey> preferenceKeys = apps.stream()
//				.map(HotpadsNotificationDeviceGroup::getForApp)
//				.filter(Optional::isPresent)
//				.map(Optional::get)
//				.map(deviceGroup -> deviceGroup.deviceGroup)
//				.map(deviceGroup -> new NotificationPreferenceKey(userToken, deviceGroup, typeGroup))
//				.collect(Collectors.toSet());
//		if(preferenceKeys.size() == 0){
//			return new HashSet<>();
//		}
//
//		//check the DB and turn each matching NotificationPreference back into a Set of NotificationDestinationApps,
//		//using HotpadsNotificationDeviceGroup to translate the keys to groups of NotificationDestinationApps
//		Set<NotificationDestinationApp> optedOutApps = notificationNodes.getNotificationPreference().getMulti(
//				preferenceKeys, null)
//				.stream()
//				.map(NotificationPreference::getKey)
//				.map(NotificationPreferenceKey::getDeviceGroup)
//				.map(deviceGroup -> HotpadsNotificationDeviceGroup.fromPersistentString(deviceGroup.persistentString))
//				.map(HotpadsNotificationDeviceGroup::getApps)
//				.flatMap(Arrays::stream)
//				.collect(Collectors.toSet());
//		return optedOutApps;
//	}

	//TODO some of these would be more appropriate in the client side
//	@Guice(moduleFactory = ServicesModuleFactory.class)
//	public static class NotificationServiceIntegrationTests{
//
//		@Inject
//		public NotificationService notificationService;
//		@Inject
//		public NotificationDao notificationDao;
//		@Inject
//		private NotificationNodes notificationNodes;
//		@Inject
//		public Datarouter datarouter;
//
//		private NotificationUserId userId = new NotificationUserId(NotificationUserType.HOTPADS_TOKEN,
//				"testUserLogItems");
//
//		private static final String FIND_TIMING_FAILED = "Failed to find NotificationTimingStrategy from key";
//
//		@Test
//		public void logItemsTest(){
//			NotificationRequest request1 = new NotificationRequest(userId, SavedSearchNotificationType.class,
//					"testDataLogItems", null);
//			NotificationRequest request2 = new NotificationRequest(userId, RecommendedSearchNotificationType.class,
//					"testDataLogItems", null);
//			notificationService.logItems(Arrays.asList(request1), Arrays.asList(UuidTool.generateV1Uuid()));
//			List<String> notificationIds = Arrays.asList(
//					UuidTool.generateV1Uuid(),
//					UuidTool.generateV1Uuid(),
//					UuidTool.generateV1Uuid(),
//					UuidTool.generateV1Uuid(),
//					UuidTool.generateV1Uuid(),
//					UuidTool.generateV1Uuid(),
//					UuidTool.generateV1Uuid());
//			notificationService.logItems(Arrays.asList(request2), notificationIds);
//		}
//
//		@Test
//		public void testGetTiming(){
//			//set up timings
//			NotificationTimingStrategy
//					one = new NotificationTimingStrategy("one", 0L, 0L, 0L, 0L, 0L, 0L, 0L),
//					two = new NotificationTimingStrategy("two", 0L, 0L, 0L, 0L, 0L, 0L, 0L),
//					three = new NotificationTimingStrategy("three", 0L, 0L, 0L, 0L, 0L, 0L, 0L),
//					four = new NotificationTimingStrategy("four", 0L, 0L, 0L, 0L, 0L, 0L, 0L);
//
//			notificationNodes.getNotificationTimingStrategy().putMulti(Arrays.asList(one, two, three, four), null);
//
//			//set up mappings
//			final String sharedType = "same";
//			NotificationTimingStrategyMapping
//					noPrefix = new NotificationTimingStrategyMapping(sharedType, "", "one"),
//					shortPrefix = new NotificationTimingStrategyMapping(sharedType, "fight", "two"),
//					longPrefix = new NotificationTimingStrategyMapping(sharedType, "fighter", "three"),
//					otherType = new NotificationTimingStrategyMapping("other", "", "four"),
//					emptyTiming = new NotificationTimingStrategyMapping("emptyTiming", "", ""),
//					missingTiming = new NotificationTimingStrategyMapping("missingTiming", "", "nonexistent");
//
//			notificationNodes.getNotificationTimingStrategyMapping().putMulti(Arrays.asList(noPrefix, shortPrefix,
//					longPrefix, otherType, emptyTiming, missingTiming), null);
//
//			//test prefix behavior
//			Assert.assertEquals(one,
//					notificationDao.getTiming(new NotificationTimingStrategyMappingKey(sharedType, "")));
//			Assert.assertEquals(one,
//					notificationDao.getTiming(new NotificationTimingStrategyMappingKey(sharedType,
//							"weirdchannel")));
//			Assert.assertEquals(two,
//					notificationDao.getTiming(new NotificationTimingStrategyMappingKey(sharedType, "fight")));
//			Assert.assertEquals(two,
//					notificationDao.getTiming(new NotificationTimingStrategyMappingKey(sharedType, "fights")));
//			Assert.assertEquals(three,
//					notificationDao.getTiming(new NotificationTimingStrategyMappingKey(sharedType, "fighter")));
//			Assert.assertEquals(three,
//					notificationDao.getTiming(new NotificationTimingStrategyMappingKey(sharedType, "fighters")));
//			Assert.assertEquals(four,
//					notificationDao.getTiming(new NotificationTimingStrategyMappingKey("other", "anything")));
//			Assert.assertEquals(four,
//					notificationDao.getTiming(new NotificationTimingStrategyMappingKey("other", null)));
//
//			//test behavior when mapping doesn't point to anything or is missing
//			try{
//				notificationDao.getTiming(new NotificationTimingStrategyMappingKey("emptyTiming", ""));
//			}catch(RuntimeException e){
//				Assert.assertEquals(e.getMessage().startsWith(FIND_TIMING_FAILED), true);
//			}
//			try{
//				notificationDao.getTiming(new NotificationTimingStrategyMappingKey("missingTiming", ""));
//			}catch(RuntimeException e){
//				Assert.assertEquals(e.getMessage().startsWith(FIND_TIMING_FAILED), true);
//			}
//			try{
//				notificationDao.getTiming(new NotificationTimingStrategyMappingKey("noMapping", ""));
//			}catch(RuntimeException e){
//				Assert.assertEquals(e.getMessage().startsWith(FIND_TIMING_FAILED), true);
//			}
//
//			//remove everything
//			notificationNodes.getNotificationTimingStrategy().deleteMulti(
//					Arrays.asList(one.getKey(), two.getKey(), three.getKey(), four.getKey()), null);
//			notificationNodes.getNotificationTimingStrategyMapping().deleteMulti(Arrays.asList(noPrefix.getKey(),
//					shortPrefix.getKey(), longPrefix.getKey(), otherType.getKey(), emptyTiming.getKey(),
//					missingTiming.getKey()), null);
//		}
//
//		@AfterSuite
//		public void afterSuite(){
//			NotificationItemLogKey prefix = new NotificationItemLogKey(userId, null, null, null);
//			Range<NotificationItemLogKey> range = new Range<>(prefix, true, prefix, true);
//			for(NotificationItemLogKey key : notificationNodes.getNotificationItemLog().scanKeys(range, null)){
//				notificationNodes.getNotificationItemLog().delete(key, null);
//			}
//
//			datarouter.shutdown();
//		}
//
//	}

}
