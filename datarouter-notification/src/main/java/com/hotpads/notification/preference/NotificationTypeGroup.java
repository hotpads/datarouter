package com.hotpads.notification.preference;

public class NotificationTypeGroup{
	public static class F {
		public static final String persistentName = "persistentName";
	}

	public final String persistentString;

	public NotificationTypeGroup(String persistentString){
		this.persistentString = persistentString;
	}
}
