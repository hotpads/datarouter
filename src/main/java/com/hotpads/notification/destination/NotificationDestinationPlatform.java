package com.hotpads.notification.destination;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum NotificationDestinationPlatform implements StringEnum<NotificationDestinationPlatform>{
	GCM("gcm"),
	ANS("ans"),
	SMS("sms"),
	EMAIL("email"),
	;

	private String persistentString;

	NotificationDestinationPlatform(String persistentString){
		this.persistentString = persistentString;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public NotificationDestinationPlatform fromPersistentString(String s){
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}

}
