package com.hotpads.notification.alias;

public enum HotPadsNotificationAlias{
	RENTAL_FEED_ALERT("RentalFeedAlert"),
	EMAIL_STAT_TEAM("Email stat"),
	;

	private NotificationAlias alias;

	private HotPadsNotificationAlias(String name){
		this.alias = new NotificationAlias(name);
	}

	public NotificationAlias getAlias(){
		return alias;
	}

}
