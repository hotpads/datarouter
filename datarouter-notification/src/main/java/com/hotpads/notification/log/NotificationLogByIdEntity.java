package com.hotpads.notification.log;

import com.hotpads.datarouter.storage.entity.BaseEntity;

public class NotificationLogByIdEntity extends BaseEntity<NotificationLogByIdEntityKey>{

	public static final String QUALIFIER_PREFIX_NOTIFICATION_LOG_BY_ID = "NLBI";

	public NotificationLogByIdEntity(NotificationLogByIdEntityKey key){
		super(key);
	}

}
