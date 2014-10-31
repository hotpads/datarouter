package com.hotpads.notification.alias;

public class NotificationAlias{

	public static class F {
		public static final String
			name = "name";
	}

	private String name;

	public NotificationAlias(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

}
