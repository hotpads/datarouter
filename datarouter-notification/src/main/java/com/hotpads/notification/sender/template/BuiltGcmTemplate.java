package com.hotpads.notification.sender.template;

import com.google.gson.JsonObject;
import com.hotpads.notification.sender.NotificationSendingAction;
import com.hotpads.notification.sender.SenderType;

public class BuiltGcmTemplate extends BaseBuiltTemplate{

	public NotificationSendingAction notificationSendingAction;//TODO move
	public JsonObject jsonData;
	public String notificationText;
	public Integer badgeCount;
	public String clickAction;
	public String gcmKey;
	public Boolean isIos;

	//TODO why are these public?

	public BuiltGcmTemplate(NotificationSendingAction notificationSendingAction, JsonObject jsonData,
			String notificationText, Integer badgeCount, String clickAction, String gcmKey, Boolean isIos){
		super(SenderType.GCM);
		this.notificationSendingAction = notificationSendingAction;
		this.jsonData = jsonData;
		this.notificationText = notificationText;
		this.badgeCount = badgeCount;
		this.clickAction = clickAction;
		this.gcmKey = gcmKey;
		this.isIos = isIos;
	}

	public NotificationSendingAction getNotificationSendingAction(){
		return notificationSendingAction;
	}

	public JsonObject getJsonData(){
		return jsonData;
	}

	public String getNotificationText(){
		return notificationText;
	}

	public Integer getBadgeCount(){
		return badgeCount;
	}

	public String getClickAction(){
		return clickAction;
	}

	public String getGcmKey(){
		return gcmKey;
	}

	public Boolean getIsIos(){
		return isIos;
	}
}