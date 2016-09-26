package com.hotpads.notification.preference;

public class NotificationTypeGroup{
	public static class F {
		public static final String
		persistentName = "persistentName";
	}

	private String persistentName;

	public NotificationTypeGroup(String persistentName){
		this.persistentName = persistentName;
	}

	public String getPersistentName(){
		return persistentName;
	}
}
