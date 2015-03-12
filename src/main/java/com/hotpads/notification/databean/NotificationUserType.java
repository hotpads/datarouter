package com.hotpads.notification.databean;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum NotificationUserType implements StringEnum<NotificationUserType> {

	HOTPADS_TOKEN("token"),
	EMAIL("email"),
	PHONE("phone"),
	ALIAS("alias"),
	;

	private String name;

	private NotificationUserType(String name) {
		this.name = name;
	}

	@Override
	public String getPersistentString() {
		return name;
	}

	@Override
	public NotificationUserType fromPersistentString(String s) {
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}

}
