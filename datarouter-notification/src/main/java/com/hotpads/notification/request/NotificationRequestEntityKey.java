package com.hotpads.notification.request;

import com.hotpads.notification.databean.BaseNotificationUserIdEntityKey;
import com.hotpads.notification.databean.NotificationUserId;

public class NotificationRequestEntityKey extends BaseNotificationUserIdEntityKey<NotificationRequestEntityKey>{

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationRequestEntityKey(){
		this(new NotificationUserId(null, null));
	}

	public NotificationRequestEntityKey(NotificationUserId userId){
		super(userId);
	}

}