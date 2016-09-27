package com.hotpads.notification.alias;

public class NotificationAlias{

	public static class F {
		public static final String
		persistentName = "persistentName";
	}

	private final String persistentName;
	private final String displayName;
	private final boolean omitReplyTo ;

	public NotificationAlias(String persistentName){
		this(persistentName, persistentName);
	}

	public NotificationAlias(String persistentName, String displayName){
		this(persistentName, displayName, false);
	}

	public NotificationAlias(String persistentName, boolean omitReplyTo){
		this(persistentName, persistentName, omitReplyTo);
	}

	private NotificationAlias(String persistentName, String displayName, boolean omitReplyTo){
		this.persistentName = persistentName;
		this.displayName = displayName;
		this.omitReplyTo = omitReplyTo;
	}

	public String getPersistentName(){
		return persistentName;
	}

	public String getDisplayName(){
		return displayName;
	}

	public boolean omitReplyTo(){
		return omitReplyTo;
	}

}
