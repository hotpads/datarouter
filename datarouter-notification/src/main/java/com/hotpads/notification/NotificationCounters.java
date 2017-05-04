package com.hotpads.notification;

import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.notification.sender.NotificationSender;

public class NotificationCounters{

	public static final String PREFIX = "Notification";

	public static void inc(String key){
		inc(key, 1L);
	}

	public static void inc(String key, long delta){
		Counters.inc(PREFIX + " " + key, delta);
	}

	public static void sendAttempt(String typeName, String appName, Class<? extends NotificationSender> senderClass,
			String templateName){
		send("attempt", typeName, appName, senderClass, templateName);
	}

	public static void sendSuccess(String typeName, String appName, Class<? extends NotificationSender> senderClass,
			String templateName){
		send("success", typeName, appName, senderClass, templateName);
	}

	public static void sendFailed(String typeName, String appName, Class<? extends NotificationSender> senderClass,
			String templateName){
		send("failed", typeName, appName, senderClass, templateName);
	}

	//TODO test strings and spaces
	public static void send(String description, String typeName, String appName,
			Class<? extends NotificationSender> senderClass, String templateName){
		//TODO template class simple name template.getClass().getSimpleName() => clientId?
		String prefix = "send " + description + " ";
		NotificationCounters.inc(prefix.trim());
		NotificationCounters.inc(prefix + typeName);
		NotificationCounters.inc(prefix + typeName + " " + appName);
		NotificationCounters.inc(prefix + senderClass.getSimpleName());//TODO might need to combine New/Old for this...
		NotificationCounters.inc(prefix + senderClass.getSimpleName() + " " + appName);
		NotificationCounters.inc(prefix + templateName);
		NotificationCounters.inc(prefix + templateName + " " + appName);
	}
}
