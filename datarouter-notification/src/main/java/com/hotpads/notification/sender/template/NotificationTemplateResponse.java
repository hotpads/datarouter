package com.hotpads.notification.sender.template;

import com.hotpads.notification.sender.SenderType;

public class NotificationTemplateResponse{//TODO names are terrible
	private BaseBuiltTemplate sendable;
	private SenderType notificationSenderType;

	public NotificationTemplateResponse(BaseBuiltTemplate sendable){
		this.sendable = sendable;
	}

	public BaseBuiltTemplate getSendable(){
		return sendable;
	}
}