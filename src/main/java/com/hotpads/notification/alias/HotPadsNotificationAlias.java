package com.hotpads.notification.alias;

public enum HotPadsNotificationAlias{
	RENTAL_FEED_ALERT("RentalFeedAlert"),
	;

	private NotificationAlias alias;

	private HotPadsNotificationAlias(String name){
		this.alias = new NotificationAlias(name);
	}

	public NotificationAlias getAlias(){
		return alias;
	}

}
