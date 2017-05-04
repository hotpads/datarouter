package com.hotpads.notification.sender.template;

import com.hotpads.notification.sender.SenderType;

public class BuiltWebsocketTemplate extends BaseBuiltTemplate{

	public String userToken;
	public Long sessionId;
	public String messageType;
	public String notificationId;
	public Object payload;
	//TODO why are these public?

	public BuiltWebsocketTemplate(String userToken, Long sessionId, String messageType, String notificationId,
			Object payload){
		super(SenderType.WEBSOCKET);
		this.userToken = userToken;
		this.sessionId = sessionId;
		this.messageType = messageType;
		this.notificationId = notificationId;
		this.payload = payload;
	}

	public String getUserToken(){
		return userToken;
	}

	public Long getSessionId(){
		return sessionId;
	}

	public String getMessageType(){
		return messageType;
	}

	public String getNotificationId(){
		return notificationId;
	}

	public Object getPayload(){
		return payload;
	}
}