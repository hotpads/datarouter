package com.hotpads.notification.sender;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.gson.JsonObject;
import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.external.gcm.json.GcmJsonSender;
import com.hotpads.external.gcm.json.GcmNotification;
import com.hotpads.external.gcm.json.GcmRequest;
import com.hotpads.external.gcm.json.GcmResponse;
import com.hotpads.external.gcm.json.GcmResult;
import com.hotpads.external.gcm.json.GcmResult.GcmResultError;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationService;
import com.hotpads.notification.result.NotificationSendingResult;
import com.hotpads.notification.sender.template.BaseBuiltTemplate;
import com.hotpads.notification.sender.template.BuiltGcmTemplate;

public class NewGcmNotificationSender implements NewNotificationSender{

	@Inject
	private NotificationDestinationService notificationDestinationService;

	@Override
	public boolean send(BaseBuiltTemplate builtTemplate, NotificationDestination notificationDestination,
			NotificationUserId notificationUserId, NotificationSendingResult notificationResult){
		BuiltGcmTemplate template = (BuiltGcmTemplate)builtTemplate;

		String deviceId = notificationDestination.getKey().getDeviceId();
		NotificationSendingAction action = template.getNotificationSendingAction();
		if(action == NotificationSendingAction.DISCARD){
			return false;
		}
		JsonObject jsonData = template.getJsonData();

		GcmNotification notification;
		if(template.getIsIos()){
			notification = new GcmNotification(template.getNotificationText(), template.getBadgeCount(),
					template.getClickAction());
		}else{
			notification = null;
		}
		GcmRequest request = new GcmRequest(deviceId, notification, jsonData);
		GcmJsonSender sender = new GcmJsonSender(template.getGcmKey());
		GcmResponse response;
//TODO remove
//		System.out.println(request.contentAvailble);
//		System.out.println(request.data);
//		System.out.println(request.priority);
//		System.out.println(request.to);
//		System.out.println(template.getNotificationText());
//		System.out.println(template.getBadgeCount());
//		System.out.println(template.getClickAction());

		try{
			response = sender.send(request, 3);
		}catch(Exception e){
			throw new RuntimeException("Exception sending notification to: " + deviceId, e);
		}
		if(response == null){
			throw new RuntimeException("result is null");
		}
		if(response.getNumFailures() > 0 && DrCollectionTool.notEmpty(response.getResults())){
			ArrayList<String> unknownErrors = new ArrayList<>();
			for(GcmResult result : response.getResults()){
				GcmResultError error = result.getGcmResultError();
				if(error == GcmResultError.NOT_REGISTERED
						|| error == GcmResultError.INVALID_REGISTRATION){
					Counters.inc("GcmSender deactivating destination " + error);
					notificationDestinationService.deactivateDestinationByTokenAndDevice(notificationDestination
							.getKey());
				}
				if(error == null){
					unknownErrors.add(result.getError());
				}
			}
			if(!unknownErrors.isEmpty()){
				throw new IllegalStateException("unknown GCM error(s): " + unknownErrors.stream()
						.collect(Collectors.joining(", ")));
			}
		}
		return true;
	}
}