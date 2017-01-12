package com.hotpads.notification.tracking;

import java.util.HashMap;
import java.util.Map;

public class NotificationTrackingEventType{

	public static class F{
		public static final String name = "name";
	}

	private static final Map<String, NotificationTrackingEventType> VALUES = new HashMap<>();

	public static final NotificationTrackingEventType
			DISPLAYED = new NotificationTrackingEventType("displayed"), //the notification was displayed to the user
			OPENED = new NotificationTrackingEventType("opened"), //the user interacted with the notification
			VISITED = new NotificationTrackingEventType("visited"), //the user land on the application
			CONVERTED = new NotificationTrackingEventType("converted"), //the client phones home when the user converts
			DISMISSED = new NotificationTrackingEventType("dismissed"); //the user dismissed the notification without
																		//looking at it
	public final String name;

	private NotificationTrackingEventType(String name){
		this.name = name;
		VALUES.put(name, this);
	}

	public static NotificationTrackingEventType createEmptyInstance(){
		return new NotificationTrackingEventType(null);
	}

	public static NotificationTrackingEventType fromString(String input){
		return VALUES.get(input);
	}

}
