package com.hotpads.notification;

import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.datarouter.util.core.DrStringTool;

public class NotificationCounters{

	public static final String PREFIX = "Notification";

	public static void inc(String key){
		inc(key, 1L);
	}

	public static void inc(String key, long delta){
		Counters.inc(PREFIX + " " + key, delta);
	}

	public static void sendAttempt(String typeName, String appName, Class<?> senderClass,
			String templateClass){
		send("attempt", typeName, appName, senderClass, templateClass);
	}

	public static void sendSuccess(String typeName, String appName, Class<?> senderClass,
			String templateClass){
		send("success", typeName, appName, senderClass, templateClass);
	}

	public static void sendFailed(String typeName, String appName, Class<?> senderClass,
			String templateClass){
		send("failed", typeName, appName, senderClass, templateClass);
	}

	public static void send(String description, String typeName, String appName,
			Class<?> senderClass, String templateClass){
		String prefix = "send " + description + " ";
		NotificationCounters.inc(prefix.trim());
		NotificationCounters.inc(prefix + typeName);
		NotificationCounters.inc(prefix + typeName + " " + appName);
		NotificationCounters.inc(prefix + senderClass.getSimpleName());
		NotificationCounters.inc(prefix + senderClass.getSimpleName() + " " + appName);
		//templateName might include package
		templateClass = DrStringTool.getSimpleClassName(templateClass);
		NotificationCounters.inc(prefix + templateClass);
		NotificationCounters.inc(prefix + templateClass + " " + appName);
	}
}
