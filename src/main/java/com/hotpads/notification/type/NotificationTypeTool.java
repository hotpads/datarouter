package com.hotpads.notification.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.notification.sender.template.NotificationTemplate;

public class NotificationTypeTool{

	public static Map<String,Class<? extends NotificationTemplate>> getTemplatesForDestination(
			NotificationType notificationType, List<NotificationDestination> destinations){
		Map<NotificationDestinationApp,Class<? extends NotificationTemplate>> templateForPlatform = notificationType
				.getTemplateForApp();
		Map<String,Class<? extends NotificationTemplate>> matchingTemplates = new HashMap<>();
		for(NotificationDestination destination : destinations){
			Class<? extends NotificationTemplate> template = templateForPlatform.get(destination.getKey().getApp());
			if(template != null){
				matchingTemplates.put(destination.getKey().getDeviceId(), template);
			}
		}
		return matchingTemplates;
	}

	public static Map<String,Class<? extends NotificationTemplate>> getTemplatesForNotificationUserId(
			NotificationType notificationType, NotificationUserId userId){
		Map<NotificationDestinationApp,Class<? extends NotificationTemplate>> templateForPlatform = notificationType
				.getTemplateForApp();
		Map<String,Class<? extends NotificationTemplate>> matchingTemplates = new HashMap<>();
		for(Entry<NotificationDestinationApp,Class<? extends NotificationTemplate>> entry : templateForPlatform
				.entrySet()){
			if(entry.getKey().accept(userId.getType())){
				matchingTemplates.put(userId.getId(), entry.getValue());
			}
		}
		return matchingTemplates;
	}

}
