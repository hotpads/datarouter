package com.hotpads.notification.alias;

public class NotificationAlias{

	public static class F {
		public static final String
		persistentName = "persistentName";
	}

	private String persistentName;
	private String displayName;

	public NotificationAlias(String persistentName){
		this.persistentName = persistentName;
		this.displayName = persistentName;
	}

	public NotificationAlias(String persistentName, String displayName){
		this.persistentName = persistentName;
		this.displayName = displayName;
	}

	public String getPersistentName(){
		return persistentName;
	}

	public String getDisplayName(){
		return displayName;
	}

}
