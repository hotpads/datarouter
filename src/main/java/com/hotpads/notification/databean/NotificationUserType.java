package com.hotpads.notification.databean;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum NotificationUserType implements StringEnum<NotificationUserType> {

	TOKEN,
	EMAIL,
	PHONE,
	ANDROID;

	@Override
	public String getPersistentString() {
		return this.toString();
	}

	@Override
	public NotificationUserType fromPersistentString(String s) {
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}

}
