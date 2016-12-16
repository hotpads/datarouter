package com.hotpads.notification.type;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.destination.NotificationDestinationApp;
import com.hotpads.util.core.lang.ClassTool;

public abstract class BaseNotificationType implements NotificationType{

	@Override
	public String getName(){
		return getClass().getName();
	}

	@Override
	public boolean isMergeableWith(NotificationType that){
		return ClassTool.sameClass(this, that);
	}

	@Override
	public List<NotificationDestinationApp> getDestinationApps(){
		return new ArrayList<>(getTemplateForApp().keySet());
	}

	@Override
	public List<NotificationRequest> filterOutIrrelevantNotificationRequests(
			List<NotificationRequest> originalRequests){
		return originalRequests;
	}

	@Override
	public String getDescription(NotificationItemLog notificationItemLog){
		return getName();
	}

	@Override
	public void onSuccess(List<NotificationRequest> requests){}

}
