package com.hotpads.notification.alias;

public enum HotPadsAutomatedEmail{
	EXPORT_SUMMARY(HotPadsNotificationAlias.RENTAL_FEED_ALERT, "Summary of feed export"),
	;

	private AutomatedEmail automatedEmail;

	private HotPadsAutomatedEmail(HotPadsNotificationAlias alias, String description){
		this.automatedEmail = new AutomatedEmail(alias.getAlias(), name(), description);
	}

	public AutomatedEmail getAutomatedEmail(){
		return automatedEmail;
	}

}
