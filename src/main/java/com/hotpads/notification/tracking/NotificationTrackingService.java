package com.hotpads.notification.tracking;

import com.hotpads.notification.databean.NotificationUserId;

public interface NotificationTrackingService{

	public static final String EMAIL_NOTIFICATION_ID_FIELD = "notificationId";

	public boolean isSignatureValid(String signature, String notificationId);

	public String generateId();
	
	public void saveSent(TrackingNotificationType type, NotificationUserId userId, String notificationId);

	String sign(String notificationId);

	void saveEvent(NotificationTrackingEventType eventType, String notificationId, String source);

}
