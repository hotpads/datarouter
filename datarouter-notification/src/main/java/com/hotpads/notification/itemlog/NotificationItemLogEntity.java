package com.hotpads.notification.itemlog;

import com.hotpads.datarouter.storage.entity.BaseEntity;

public class NotificationItemLogEntity extends BaseEntity<NotificationItemLogEntityKey>{

	public static final String QUALIFIER_PREFIX_NOTIFICATION_ITEM_LOG = "NIL";

	public NotificationItemLogEntity(NotificationItemLogEntityKey key){
		super(key);
	}

}