package com.hotpads.notification.sender;

public enum SenderType{//TODO maybe not necessary when using NotificationPlatform properly?
	SIMPLE_EMAIL, GCM, WEBSOCKET, RENDERED_EMAIL, SMS;
}