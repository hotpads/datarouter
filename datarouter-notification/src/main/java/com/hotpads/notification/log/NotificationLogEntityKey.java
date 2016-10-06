package com.hotpads.notification.log;

import com.hotpads.notification.databean.BaseNotificationUserIdEntityKey;
import com.hotpads.notification.databean.NotificationUserId;

public class NotificationLogEntityKey extends BaseNotificationUserIdEntityKey<NotificationLogEntityKey>{

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationLogEntityKey(){
		this(new NotificationUserId(null, null));
	}

	public NotificationLogEntityKey(NotificationUserId userId){
		super(userId);
	}

}