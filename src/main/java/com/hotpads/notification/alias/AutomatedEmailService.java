package com.hotpads.notification.alias;

public interface AutomatedEmailService{

	void send(AutomatedEmailType automatedEmail, String subject, String content);

	void send(AutomatedEmailType automatedEmail, String subject, String content, boolean html);

}
