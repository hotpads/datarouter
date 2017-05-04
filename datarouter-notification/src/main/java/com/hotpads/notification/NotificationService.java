package com.hotpads.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.notification.databean.NotificationDestinationApp;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationLogKey;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationRequestKey;
import com.hotpads.notification.databean.NotificationTypeConfig;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationAppName;
import com.hotpads.notification.result.NotificationFailureReason;
import com.hotpads.notification.result.NotificationSendingResult;
import com.hotpads.notification.sender.NewGcmNotificationSender;
import com.hotpads.notification.sender.NewNotificationSender;
import com.hotpads.notification.sender.NewWebsocketSender;
import com.hotpads.notification.sender.NotificationSender;
import com.hotpads.notification.sender.template.BaseBuiltTemplate;
import com.hotpads.notification.sender.template.NotificationTemplate;
import com.hotpads.notification.sender.template.NotificationTemplateFactory;
import com.hotpads.notification.sender.template.NotificationTemplateRequest;
import com.hotpads.notification.timing.NotificationTimingStrategy;
import com.hotpads.notification.timing.NotificationTimingStrategyMappingKey;
import com.hotpads.notification.tracking.NotificationTrackingService;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.profile.PhaseTimer;

@Singleton
public class NotificationService{
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	private static final String HBASE1_NAME = new ClientId("hbase1", true).getName();

	@Inject
	private DatarouterInjector injector;
	@Inject
	private NotificationTemplateFactory templateFactory;
	@Inject
	private NotificationTrackingService notificationTrackingService;
	@Inject
	private ExceptionRecorder exceptionRecorder;
	@Inject
	NotificationServiceCallbacks callbacks;
	@Inject
	private NotificationNodes notificationNodes;
	@Inject
	private ClientAvailabilitySettings availabilitySettings;
	@Inject
	private NotificationDao notificationDao;

	public List<NotificationSendingResult> sendNotifications(List<NotificationRequest> requests, String jobName){
		PhaseTimer timer = new PhaseTimer();
		NotificationRequest firstRequest = requests.get(0);
		String typeName = firstRequest.getShortType();
		NotificationTypeConfig typeConfig = notificationDao.getNotificationTypeConfig(typeName);
		if(typeConfig == null){
			throw new RuntimeException("Requested notification type name not configured: " + typeName);
		}
		//fully qualified name (package + class)
		//TODO this field can be removed after the following:
		//-the client callback implementations handle translation from NotificationTypeConfig name
		String clientId = typeConfig.getClientId();

		Map<NotificationDestinationAppName,String> appToTemplateMap = notificationDao
				.getDestinationAppToTemplateMappingForType(typeName);
		if(typeConfig.getNeedsRemoveDisabledCallback() && !appToTemplateMap.isEmpty()){
			//filter the keys, then make a new map with only the filtered entries
			appToTemplateMap = callbacks.filterOutDisabledDestinationApps(clientId, appToTemplateMap.keySet())
					.stream()
					.collect(Collectors.toMap(Function.identity(), appToTemplateMap::get));
		}
		if(appToTemplateMap.isEmpty()){
			return finishSendNotificationsWithoutSend(requests);
		}

		NotificationUserId userId = firstRequest.getKey().getNotificationUserId();
		Set<NotificationDestinationApp> destinationApps = notificationDao.getNotificationDestinationAppsByName(
				appToTemplateMap.keySet());
		Set<NotificationDestination> destinations;
		if(userId.getType().needNotificationDestinationService()){
			destinations = callbacks.getActiveDestinations(userId, appToTemplateMap.keySet());
		}else{
			destinationApps.removeIf(app -> !app.getAcceptedUserTypes().contains(userId.getType()));
			destinations = destinationApps.stream()
					.map(app -> new NotificationDestination(null, app.getKey().getName(), userId.getId()))
					.collect(Collectors.toSet());
		}

		Map<NotificationDestination,String> tempByDest = notificationDao.buildTemplateClassMap(notificationDao
				.filterOutOptedOut(typeConfig.getGroupName(), destinations, destinationApps), appToTemplateMap);
		if(tempByDest.isEmpty()){
			return finishSendNotificationsWithoutSend(requests);
		}

		List<NotificationRequest> selectedRequests = typeConfig.getNeedsFilterOutIrrelevantCallback()
				? callbacks.filterOutIrrelevantNotificationRequests(clientId, requests) : requests;
		if(selectedRequests.isEmpty()){
			return finishSendNotificationsWithoutSend(requests);
		}

		boolean sent = false;
		List<NotificationSendingResult> results = new ArrayList<>();
		List<String> sentNotificationIds = new ArrayList<>();
		for(Entry<NotificationDestination,String> entry : tempByDest.entrySet()){
			NotificationDestination notificationDestination = entry.getKey();
			String templateClass = entry.getValue();
			NotificationSendingResult result = new NotificationSendingResult(userId, templateClass);
			results.add(result);
			String uuid = notificationTrackingService.generateId();
			NotificationSender oldSender = null;
			NotificationTemplate oldTemplate = null;//TODO make sure new/old is exclusive
			NewNotificationSender newSender = null;
			BaseBuiltTemplate newTemplate = null;
			boolean shouldUseNewSender = false;//TODO get in setting
			try{
				if(shouldUseNewSender){
					try{
					newTemplate = callbacks.buildRequests(new NotificationTemplateRequest(templateClass, userId,
							notificationDestination, uuid, selectedRequests)).getSendable();
					newSender = getSender(newTemplate);
					}catch(Exception e){
						logger.error("Error creating new notification sender and template. Falling back to old. "
								+ "Template: " + templateClass, e);
						shouldUseNewSender = false;
					}
				}
				if(!shouldUseNewSender){
					oldTemplate = templateFactory.create(templateClass);
					oldSender = injector.getInstance(oldTemplate.getNotificationSender());
				}
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

			if(!shouldUseNewSender){
				oldSender.setUserId(userId, notificationDestination);
				try{
					oldSender.setTemplate(oldTemplate);
				}catch(IllegalArgumentException e){
					logger.warn(userId + " does not fit the requirements for "
							+ oldTemplate.getClass().getSimpleName() + ": " + e.getMessage());
					NotificationCounters.inc("wrong UserId");
					result.setFail(NotificationFailureReason.WRONG_USER_ID);
					remove(requests);//TODO improve multiple device
					continue;
				}
				oldTemplate.setRequests(selectedRequests);
				oldTemplate.setNotificationId(uuid);
			}
			//TODO how to recreate failure behavior above?

			String appName = notificationDestination.getKey().getApp().persistentString;
			//TODO ensure templateClass here is same as old logging
			//TODO want same sender class here or not?
			NotificationCounters.sendAttempt(typeName, appName, shouldUseNewSender ? newSender.getClass() : oldSender
					.getClass(), templateClass);
			timer.add("misc1");
			try{
				boolean senderSent;
				if(shouldUseNewSender){
					try{
						senderSent = newSender.send(newTemplate, notificationDestination, userId, result);
					}catch(IllegalArgumentException e){//TODO can this still happen?
						logger.warn(userId + " does not fit the requirements for " + templateClass + " (new send "
								+ "attempt)", e);
						NotificationCounters.inc("wrong UserId new");
						result.setFail(NotificationFailureReason.WRONG_USER_ID);
						remove(requests);//TODO improve multiple device
						continue;
					}catch(Exception e){
						logger.error("Failed to send with new sender. Template: " + templateClass, e);
						throw e;
					}
				}else{
					senderSent = oldSender.send(result);
				}
				timer.add("senderSent");
				if(senderSent){
					NotificationCounters.sendSuccess(typeName, appName, shouldUseNewSender ? newSender.getClass()
							: oldSender.getClass(), templateClass);
					sent = true;
					sentNotificationIds.add(uuid);
					//TODO only log successes (after senders/templates can have successes and failures)
					log(selectedRequests, templateClass, uuid, notificationDestination.getKey().getDeviceId());
				}else{
					result.setFailIfnotSet(NotificationFailureReason.DISCARD_BY_SENDER);
					NotificationCounters.inc("discard");
					logger.info("{} discared by the sender", typeName);
					remove(requests);//TODO improve multiple device
				}
			}catch(Exception e){
				result.setFail(NotificationFailureReason.SENDER_FAILED);
				NotificationCounters.sendFailed(typeName, appName, shouldUseNewSender ? newSender.getClass() : oldSender
						.getClass(), templateClass);
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
				callbacks.onSuccess(clientId, selectedRequests);
			}
			if(notificationNodes.getNotificationLog().areAllPhysicalNodesAvailableForWrite() || jobName != null){
				//TODO only log successes (after senders/templates can have successes and failures)
				notificationDao.logItems(selectedRequests, sentNotificationIds);
				remove(requests);
			}
		}
		timer.add("postProcess");
		logger.info(timer.toString());
		return results;
	}

	private NewNotificationSender getSender(BaseBuiltTemplate template){
		Class<? extends NewNotificationSender> senderClass;
		switch(template.getSenderType()){
		case GCM:
			senderClass = NewGcmNotificationSender.class;
			break;
		case WEBSOCKET:
			senderClass = NewWebsocketSender.class;
			break;
		default:
			throw new RuntimeException("Unknown SenderType: " + template.getSenderType());
		}
		return injector.getInstance(senderClass);
	}

	private List<NotificationSendingResult> finishSendNotificationsWithoutSend(List<NotificationRequest> requests){
		remove(requests);
		return Collections.emptyList();
	}

	//TODO ensure log with string instead of class is still the same
	private void log(List<NotificationRequest> requests, String template, String uuid, String deviceId){
		List<String> itemIds = new ArrayList<>();

		for(NotificationRequest request : requests){
			itemIds.add(request.getData());
		}
		NotificationRequest firstRequest = requests.get(0);
		NotificationLog notificationLog = new NotificationLog(firstRequest.getKey().getNotificationUserId(), new Date(),
				template, firstRequest.getType(), itemIds, firstRequest.getChannel(), uuid, deviceId);
		notificationNodes.getNotificationLog().put(notificationLog, null);
	}

	private void remove(List<NotificationRequest> requests){
		notificationNodes.getNotificationRequest().deleteMulti(DatabeanTool.getKeys(requests), null);
	}

	public int processUser(List<NotificationRequest> userRequests, String jobName){
		PhaseTimer timer = new PhaseTimer();
		List<List<NotificationRequest>> groups = new ArrayList<>();
		Map<NotificationTimingStrategyMappingKey, NotificationTimingStrategy> timingCache = new HashMap<>();
		for(NotificationRequest request : userRequests){
			processUserRequest(request, groups, timingCache);
			timer.sum("processUserRequest");
		}
		int notificationSent = 0;
		Set<NotificationRequestKey> toDelete = new HashSet<>();
		for(List<NotificationRequest> group : groups){
			boolean shouldBSent = shouldBeSent(group, timingCache);
			timer.sum("shouldBeSent");
			if(shouldBSent){
				try{
					notificationSent += NotificationSendingResult.countSuccesses(sendNotifications(group, jobName));
				}catch(Exception e){
					NotificationRequest firstInGroup = group.get(0);
					logger.warn("Error sending " + firstInGroup.getType() + " group (first notification request "
							+ firstInGroup + ").", e);
				}
				timer.sum("sendNotifications");
			}else{
				for(NotificationRequest request : group){
					if(canBeDropped(request, timingCache)){
						toDelete.add(request.getKey());
						logger.info("notification " + request + " dropped");
						NotificationCounters.inc("dropped");
					}
				}
				timer.sum("noNotifications");
			}
		}
		notificationNodes.getNotificationRequest().deleteMulti(toDelete, null);
		timer.add("deleteMulti");
		logger.info(timer.toString());
		return notificationSent;
	}

	private void processUserRequest(NotificationRequest userRequest, List<List<NotificationRequest>> groupedRequests,
			Map<NotificationTimingStrategyMappingKey, NotificationTimingStrategy> timingCache){
		String typeName = userRequest.getShortType();
		if(notificationDao.getNotificationTypeConfig(userRequest.getShortType()) == null){
			logger.warn("Requested notification type name not configured: " + typeName + ". Request: " + userRequest);
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
		String grouptype = first.getType();
		return grouptype.equals(request.getType()) && group.size() < timing.getMaxItems() && haveSameChannel(first,
				request);
	}

	private static boolean haveSameChannel(NotificationRequest first, NotificationRequest request){
		return first.getChannel() == null && request.getChannel() == null
				|| first.getChannel() != null && first.getChannel().equals(request.getChannel());
	}
}
