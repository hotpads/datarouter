package com.hotpads.notification.databean;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum NotificationUserType implements StringEnum<NotificationUserType> {

	HOTPADS_TOKEN("token", true),
	EMAIL("email", false),
	PHONE("phone", false),
	ALIAS("alias", false),
	;

	private String name;
	private boolean needNotificationDestinationService;

	private NotificationUserType(String name, boolean needNotificationDestinationService) {
		this.name = name;
		this.needNotificationDestinationService = needNotificationDestinationService;
	}

	@Override
	public String getPersistentString() {
		return name;
	}

	@Override
	public NotificationUserType fromPersistentString(String s) {
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}

	public boolean needNotificationDestinationService(){
		return needNotificationDestinationService;
	}

}
