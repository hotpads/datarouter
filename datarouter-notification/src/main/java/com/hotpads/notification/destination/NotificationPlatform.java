package com.hotpads.notification.destination;

import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;

public enum NotificationPlatform implements StringEnum<NotificationPlatform>{
	GCM("gcm"),// Google Cloud Messaging
	APNS("apns"),// Apple Push Notification Service
	SMS("sms"),
	EMAIL("email"),
	;

	private String persistentString;

	NotificationPlatform(String persistentString){
		this.persistentString = persistentString;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public NotificationPlatform fromPersistentString(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

}
