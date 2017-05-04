package com.hotpads.notification.sender;

public class WebSocketNotificationMessage{

	public final String type;
	public final String notificationId;
	public final Object payload;

	public WebSocketNotificationMessage(String type, String notificationId, Object payload){
		this.type = type;
		this.notificationId = notificationId;
		this.payload = payload;
	}

}
