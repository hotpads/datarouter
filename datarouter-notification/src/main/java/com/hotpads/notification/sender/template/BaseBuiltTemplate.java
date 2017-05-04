package com.hotpads.notification.sender.template;

import com.hotpads.notification.sender.SenderType;

public abstract class BaseBuiltTemplate{
	//TODO if one template can be sent by multiple senders in future, then this will go in a config bean
	private final SenderType senderType;

	public BaseBuiltTemplate(SenderType senderType){
		this.senderType = senderType;
	}

	public SenderType getSenderType(){
		return senderType;
	}
}