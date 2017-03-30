package com.hotpads.notification.databean;

import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;

public enum NotificationUserType implements StringEnum<NotificationUserType> {

	HOTPADS_TOKEN("token", true),
	EMAIL("email", false),
	PHONE("phone", false),
	ALIAS("alias", true),
	ZRM_TOKEN("zrmToken", true),
	;

	private String name;
	private boolean needNotificationDestinationService;

	private NotificationUserType(String name, boolean needNotificationDestinationService){
		this.name = name;
		this.needNotificationDestinationService = needNotificationDestinationService;
	}

	@Override
	public String getPersistentString(){
		return name;
	}

	@Override
	public NotificationUserType fromPersistentString(String string){
		return DatarouterEnumTool.getEnumFromString(values(), string, null);
	}

	public boolean needNotificationDestinationService(){
		return needNotificationDestinationService;
	}

}
