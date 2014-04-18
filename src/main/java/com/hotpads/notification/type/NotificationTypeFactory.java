package com.hotpads.notification.type;

import javax.inject.Singleton;

import com.hotpads.util.core.java.ReflectionTool;

@Singleton
public class NotificationTypeFactory {
	
	public NotificationType create(String typeString) throws IllegalArgumentException {
		try {
			return ReflectionTool.create(typeString);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(typeString + " is not a NotificationType known");
		}
	}

}
