package com.hotpads.notification.sender;

import com.hotpads.notification.sender.template.NotificationTemplate;

public class SenderAndTemplate<S extends NotificationSender> {

	private Class<S> sender;

	private Class<? extends NotificationTemplate<S>> template;

	public SenderAndTemplate(Class<S> sender) {
		this.sender = sender;
	}

	public void bindTemplate(Class<? extends NotificationTemplate<S>> template) {
		this.template = template;
	}

	public Class<S> getSender() {
		return sender;
	}

	public Class<? extends NotificationTemplate<S>> getTemplate() throws IllegalStateException {
		if (template == null) {
			throw new IllegalStateException("No template bound with " + sender.getSimpleName());
		}
		return template;
	}

}
