package com.hotpads.notification.sender.template;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.notification.NotificationNodes;
import com.hotpads.notification.databean.NotificationTemplateBean;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNotificationTemplate extends Cached<Map<String, String>>{

	private final NotificationNodes notificationNodes;

	@Inject
	public CachedNotificationTemplate(NotificationNodes notificationNodes){
		super(15, TimeUnit.SECONDS);
		this.notificationNodes = notificationNodes;
	}

	@Override
	protected Map<String, String> reload(){
		return notificationNodes.getNotificationTemplate()
				.stream(null, null)
				.collect(Collectors.toMap(bean -> bean.getKey().getName(), NotificationTemplateBean::getFullClassName));
	}
}
