package com.hotpads.notification.destination;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum NotificationDestinationAppEnum
implements StringEnum<NotificationDestinationAppEnum>, NotificationDestinationApp{
	HOTPADS_ANDROID("hotpads_android", NotificationDestinationPlatform.GCM),
	;

	private String persistentString;
	private NotificationDestinationPlatform platform;

	private NotificationDestinationAppEnum(String persistentString, NotificationDestinationPlatform platform){
		this.persistentString = persistentString;
		this.platform = platform;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public NotificationDestinationAppEnum fromPersistentString(String s){
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}

	@Override
	public NotificationDestinationPlatform getPlatform(){
		return platform;
	}

}
