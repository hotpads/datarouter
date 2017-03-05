package com.hotpads.notification.sender.template;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.util.core.java.ReflectionTool;

public class NotificationTemplateFactory{

	@Inject
	private DatarouterInjector injector;

	private final Map<String, Class<? extends NotificationTemplate>> classByNameCache = new HashMap<>();

	public NotificationTemplate create(String typeString){
		Class<? extends NotificationTemplate> notificationTypeClass = getClass(typeString);
		return injector.getInstance(notificationTypeClass);
	}

	private Class<? extends NotificationTemplate> getClass(String typeString){
		return classByNameCache.computeIfAbsent(typeString, $ -> ReflectionTool.getAsSubClass(typeString,
				NotificationTemplate.class));
	}
}
