package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;

public enum HotpadsNotificationDestinationApp
implements StringEnum<HotpadsNotificationDestinationApp>{
	@Deprecated
	HOTPADS_GCM("hotpads_gcm", NotificationDestinationPlatform.GCM),
	HOTPADS_IOS_GCM("hotpads_ios_gcm", NotificationDestinationPlatform.GCM),
	HOTPADS_ANDROID_GCM("hotpads_android_gcm", NotificationDestinationPlatform.GCM),
	HOTPADS_BROWSER_GCM("hotpads_browser_gcm", NotificationDestinationPlatform.GCM),
	HOTPADS_EMAIL("hotpads_email", NotificationDestinationPlatform.EMAIL, NotificationUserType.EMAIL),
	HOTPADS_SMS("hotpads_sms", NotificationDestinationPlatform.SMS, NotificationUserType.PHONE),
	ZRM_IOS_GCM("zrm_ios_gcm", NotificationDestinationPlatform.GCM),
	ZRM_ANDROID_GCM("zrm_android_gcm", NotificationDestinationPlatform.GCM);

	private NotificationDestinationApp app;
	private NotificationDestinationPlatform platform;
	private Set<NotificationUserType> acceptedAutologicalUserTypes;

	private HotpadsNotificationDestinationApp(String persistentString, NotificationDestinationPlatform platform,
			NotificationUserType... userTypes){
		this.app = new NotificationDestinationApp(persistentString);
		this.platform = platform;
		this.acceptedAutologicalUserTypes = new HashSet<>(Arrays.asList(userTypes));
	}

	@Override
	public String getPersistentString(){
		return app.persistentString;
	}

	@Override
	public HotpadsNotificationDestinationApp fromPersistentString(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

	public NotificationDestinationPlatform getPlatform(){
		return platform;
	}

	public boolean accept(NotificationUserType type){
		return acceptedAutologicalUserTypes.contains(type);
	}

	public NotificationDestinationApp getApp(){
		return app;
	}

	public static HotpadsNotificationDestinationApp fromApp(NotificationDestinationApp app){
		return HotpadsNotificationDestinationApp.HOTPADS_ANDROID_GCM.fromPersistentString(app.persistentString);
	}
}