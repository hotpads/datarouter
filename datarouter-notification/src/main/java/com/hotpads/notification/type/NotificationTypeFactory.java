package com.hotpads.notification.type;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.util.core.java.ReflectionTool;

@Singleton
public class NotificationTypeFactory{

	@Inject
	private DatarouterInjector injector;

	private final Map<String,Class<? extends NotificationType>> classByNameCache = new HashMap<>();

	public NotificationType create(String typeString){
		Class<? extends NotificationType> notificationTypeClass = getClass(typeString);
		return injector.getInstance(notificationTypeClass);
	}

	private Class<? extends NotificationType> getClass(String typeString){
		return classByNameCache.computeIfAbsent(typeString, $ -> ReflectionTool.getAsSubClass(typeString,
				NotificationType.class));
	}

}
