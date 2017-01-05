package com.hotpads.notification.destination;

import com.hotpads.notification.databean.NotificationUserType;

public interface NotificationDestinationApp{

	NotificationDestinationPlatform getPlatform();

	boolean accept(NotificationUserType type);

	public String getPersistentString();

	public NotificationDestinationApp fromPersistentString(String str);
}
