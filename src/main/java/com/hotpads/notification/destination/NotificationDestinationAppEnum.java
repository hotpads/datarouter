package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.notification.databean.NotificationUserType;

public enum NotificationDestinationAppEnum
implements StringEnum<NotificationDestinationAppEnum>, NotificationDestinationApp{
	HOTPADS_ANDROID("hotpads_android", NotificationDestinationPlatform.GCM),
	HOTPADS_EMAIL("hotpads_email", NotificationDestinationPlatform.EMAIL, NotificationUserType.EMAIL),
	HOTPADS_SMS("hotpads_sms", NotificationDestinationPlatform.SMS, NotificationUserType.PHONE)
	;

	private String persistentString;
	private NotificationDestinationPlatform platform;
	private Set<NotificationUserType> acceptedAutologicalUserTypes;

	private NotificationDestinationAppEnum(String persistentString, NotificationDestinationPlatform platform,
			NotificationUserType... userTypes){
		this.persistentString = persistentString;
		this.platform = platform;
		this.acceptedAutologicalUserTypes = new HashSet<>(Arrays.asList(userTypes));
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

	@Override
	public boolean accept(NotificationUserType type){
		return acceptedAutologicalUserTypes.contains(type);
	}

}
