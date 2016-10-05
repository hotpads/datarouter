package com.hotpads.notification.preference;

public class NotificationDeviceGroup{
	public static class F {
		public static final String persistentString = "persistentString";
	}

	public final String persistentString;

	public NotificationDeviceGroup(String persistentString){
		this.persistentString = persistentString;
	}

	public NotificationDeviceGroup(){
		this(null);
	}
}
