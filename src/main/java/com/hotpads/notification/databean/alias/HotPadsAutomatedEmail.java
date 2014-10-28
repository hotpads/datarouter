package com.hotpads.notification.databean.alias;

public enum HotPadsAutomatedEmail{
	EXPORT_SUMMARY(HotPadsNotificationAlias.RENTAL_FEED_ALERT.getAlias(), "Summary of feed export"),
	;

    private AutomatedEmail automatedEmail;

    private HotPadsAutomatedEmail(NotificationAlias alias, String description){
        this.automatedEmail = new AutomatedEmail(alias, description);
    }

}
