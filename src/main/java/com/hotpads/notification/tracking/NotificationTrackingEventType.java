package com.hotpads.notification.tracking;

public class NotificationTrackingEventType{

	public static class F {
		public static final String
			name = "name";
	}

	public static final	NotificationTrackingEventType
			OPENED = new NotificationTrackingEventType("opened"),
			VISITED = new NotificationTrackingEventType("visited"),
			CONVERTED = new NotificationTrackingEventType("converted");

	private String name;

	private NotificationTrackingEventType(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public static NotificationTrackingEventType createEmptyInstance(){
		return new NotificationTrackingEventType(null);
	}

}