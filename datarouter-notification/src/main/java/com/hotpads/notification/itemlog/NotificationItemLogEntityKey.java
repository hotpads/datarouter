package com.hotpads.notification.itemlog;

import com.hotpads.notification.databean.BaseNotificationUserIdEntityKey;
import com.hotpads.notification.databean.NotificationUserId;

@SuppressWarnings("serial")
public class NotificationItemLogEntityKey extends BaseNotificationUserIdEntityKey<NotificationItemLogEntityKey>{

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationItemLogEntityKey(){
		this(new NotificationUserId(null, null));
	}

	public NotificationItemLogEntityKey(NotificationUserId userId){
		super(userId);
	}

}