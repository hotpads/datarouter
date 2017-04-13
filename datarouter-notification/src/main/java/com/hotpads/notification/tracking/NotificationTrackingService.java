package com.hotpads.notification.tracking;

import com.hotpads.notification.databean.NotificationUserId;

public interface NotificationTrackingService{

	public static final String EMAIL_NOTIFICATION_ID_FIELD = "notificationId";
	public static final String EMAIL_SIGNATURE_FIELD = "signature";
	public static final String EMAIL_TRACKING_SERVER_NAME_FIELD = "trackingServerName";

	public boolean isSignatureValid(String signature, String notificationId);

	public String generateId();

	public void saveSent(TrackingNotificationType type, NotificationUserId userId, String notificationId,
			String deviceId);

	void saveEvent(NotificationTrackingEventType eventType, String notificationId, String source);

}
