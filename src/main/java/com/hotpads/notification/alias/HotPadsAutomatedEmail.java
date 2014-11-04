package com.hotpads.notification.alias;

public enum HotPadsAutomatedEmail{
	EXPORT_SUMMARY(HotPadsNotificationAlias.RENTAL_FEED_ALERT, "Summary of feed export"),
	EMAIL_STAT_EXPORT(HotPadsNotificationAlias.EMAIL_STAT_TEAM, "Alert and links to the exported data about last day emails"),
	;

	private AutomatedEmail automatedEmail;

	private HotPadsAutomatedEmail(HotPadsNotificationAlias alias, String description){
		this.automatedEmail = new AutomatedEmail(alias.getAlias(), name(), description);
	}

	public AutomatedEmail getAutomatedEmail(){
		return automatedEmail;
	}

}
