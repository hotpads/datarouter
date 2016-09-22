package com.hotpads.notification.alias;

public interface AutomatedEmailService{

	void send(AutomatedEmailType automatedEmail, String subject, String content);

	void send(AutomatedEmailType automatedEmail, String subject, String content, boolean html);

	void send(AutomatedEmailType automatedEmail, String subject, String content, String groupingKey);

	void send(AutomatedEmailType automatedEmail, String subject, String content, String groupingKey, boolean html);

}
