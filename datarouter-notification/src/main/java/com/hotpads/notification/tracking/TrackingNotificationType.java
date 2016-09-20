package com.hotpads.notification.tracking;

public class TrackingNotificationType{

	public static final class F{
		public static final String name = "name";
	}

	public final String name;

	public TrackingNotificationType(){
		name = null;
	}

	public TrackingNotificationType(String name){
		this.name = name;
	}

}
