package com.hotpads.notification.alias;

public enum HotPadsNotificationAlias{
	RENTAL_FEED_ALERT("RentalFeedAlert"),
	EMAIL_STAT_TEAM("Email Stat Team"),
	;

	private NotificationAlias alias;

	private HotPadsNotificationAlias(String name){
		this.alias = new NotificationAlias(name);
	}

	public NotificationAlias getAlias(){
		return alias;
	}

}
