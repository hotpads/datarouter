package com.hotpads.notification.preference;

public class NotificationDeviceGroup{
	public static class F {
		public static final String
		persistentName = "persistentName";
	}

	private String persistentName;

	public NotificationDeviceGroup(String persistentName){
		this.persistentName = persistentName;
	}

	public String getPersistentName(){
		return persistentName;
	}
}
