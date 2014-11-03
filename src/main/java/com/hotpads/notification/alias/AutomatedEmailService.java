package com.hotpads.notification.alias;

public interface AutomatedEmailService{

	void send(NotificationAlias alias, String content, String subject);

}
