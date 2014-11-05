package com.hotpads.notification.alias;

public enum HotPadsAutomatedEmailType{
	EXPORT_SUMMARY(HotPadsNotificationAlias.RENTAL_FEED_ALERT, "Summary of feed export"),
	EMAIL_STAT_EXPORT(HotPadsNotificationAlias.EMAIL_STAT_TEAM, "Alert and links to the exported data about last day emails"),
	;

	private AutomatedEmailType automatedEmail;

	private HotPadsAutomatedEmailType(HotPadsNotificationAlias alias, String description){
		this.automatedEmail = new AutomatedEmailType(alias.getAlias(), name(), description);
	}

	public AutomatedEmailType getAutomatedEmail(){
		return automatedEmail;
	}

}
