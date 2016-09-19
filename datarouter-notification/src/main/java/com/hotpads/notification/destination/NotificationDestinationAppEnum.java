package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.notification.databean.NotificationUserType;

public enum NotificationDestinationAppEnum
implements StringEnum<NotificationDestinationAppEnum>, NotificationDestinationApp{
	@Deprecated
	HOTPADS_GCM("hotpads_gcm", NotificationDestinationPlatform.GCM),
	HOTPADS_IOS_GCM("hotpads_ios_gcm", NotificationDestinationPlatform.GCM),
	HOTPADS_ANDROID_GCM("hotpads_android_gcm", NotificationDestinationPlatform.GCM),
	HOTPADS_BROWSER_GCM("hotpads_browser_gcm", NotificationDestinationPlatform.GCM),
	HOTPADS_EMAIL("hotpads_email", NotificationDestinationPlatform.EMAIL, NotificationUserType.EMAIL),
	HOTPADS_SMS("hotpads_sms", NotificationDestinationPlatform.SMS, NotificationUserType.PHONE),
	ZRM_IOS_GCM("zrm_ios_gcm", NotificationDestinationPlatform.GCM),
	ZRM_ANDROID_GCM("zrm_android_gcm", NotificationDestinationPlatform.GCM),
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
