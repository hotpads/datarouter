package com.hotpads.notification.preference;

public class NotificationDeviceGroup{
	public static class F {
		public static final String persistentName = "persistentName";
	}

	private String persistentString;

	public NotificationDeviceGroup(String persistentString){
		this.persistentString = persistentString;
	}

	public String getPersistentString(){
		return persistentString;
	}
}
