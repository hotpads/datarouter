package com.hotpads.notification.databean.alias;

public enum HotPadsNotificationAlias{
	RENTAL_FEED_ALERT("RentalFeedAlert"),
	;

	private NotificationAlias alias;

	private HotPadsNotificationAlias(String name){
		this.alias = new NotificationAlias(name);
	}

}
