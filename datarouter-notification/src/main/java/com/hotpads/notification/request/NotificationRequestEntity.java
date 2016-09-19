package com.hotpads.notification.request;

import com.hotpads.datarouter.storage.entity.BaseEntity;

public class NotificationRequestEntity extends BaseEntity<NotificationRequestEntityKey>{

	public static final String QUALIFIER_PREFIX_NOTIFICATION_REQUEST = "NR";

	public NotificationRequestEntity(NotificationRequestEntityKey key){
		super(key);
	}

}