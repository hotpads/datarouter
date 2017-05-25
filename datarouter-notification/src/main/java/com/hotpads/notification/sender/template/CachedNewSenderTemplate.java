package com.hotpads.notification.sender.template;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.NotificationNodes;
import com.hotpads.notification.databean.NotificationTemplateBean;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNewSenderTemplate extends Cached<Set<String>>{

	private final NotificationNodes notificationNodes;

	@Inject
	public CachedNewSenderTemplate(NotificationNodes notificationNodes){
		super(15, TimeUnit.SECONDS);
		this.notificationNodes = notificationNodes;
	}

	@Override
	protected Set<String> reload(){
		return notificationNodes.getNotificationTemplate()
				.stream(null, null)
				.filter(template -> template.getShouldUseNewSender() != null)
				.filter(NotificationTemplateBean::getShouldUseNewSender)
				.map(NotificationTemplateBean::getFullClassName)
				.collect(Collectors.toSet());
	}
}
