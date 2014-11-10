package com.hotpads.notification.tracking;

public class NotificationTrackingEventType{

	public static class F {
		public static final String
			name = "name";
	}

	public static final	NotificationTrackingEventType
			OPEN = new NotificationTrackingEventType("open"),
			VISTED = new NotificationTrackingEventType("visited"),
			INQUIRIED = new NotificationTrackingEventType("inquiried");

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
