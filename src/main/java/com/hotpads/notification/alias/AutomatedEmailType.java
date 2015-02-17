package com.hotpads.notification.alias;

public class AutomatedEmailType{

	private NotificationAlias alias;
	private String name;
    private String description;

    public AutomatedEmailType(NotificationAlias alias, String name, String description){
		this.alias = alias;
		this.name = name;
		this.description = description;
	}

    public NotificationAlias getAlias(){
		return alias;
	}

    public String getName(){
		return name;
	}

}