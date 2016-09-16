package com.hotpads.notification.log;

import com.hotpads.datarouter.storage.entity.BaseEntity;

public class NotificationLogEntity extends BaseEntity<NotificationLogEntityKey>{

	public static final String QUALIFIER_PREFIX_NOTIFICATION_LOG = "NL";

	public NotificationLogEntity(NotificationLogEntityKey key){
		super(key);
	}

}