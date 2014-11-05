package com.hotpads.notification.alias;

public interface AutomatedEmailService{

	void send(AutomatedEmailType automatedEmail, String content, String subject);

}
