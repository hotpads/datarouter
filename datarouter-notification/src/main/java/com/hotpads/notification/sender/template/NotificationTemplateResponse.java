package com.hotpads.notification.sender.template;

public class NotificationTemplateResponse{
	private BaseBuiltTemplate sendable;

	public NotificationTemplateResponse(BaseBuiltTemplate sendable){
		this.sendable = sendable;
	}

	public BaseBuiltTemplate getSendable(){
		return sendable;
	}
}