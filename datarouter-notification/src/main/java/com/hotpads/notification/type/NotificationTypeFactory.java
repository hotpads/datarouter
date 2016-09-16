package com.hotpads.notification.type;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.inject.DatarouterInjector;

@Singleton
public class NotificationTypeFactory{

	@Inject
	private DatarouterInjector injector;

	public NotificationType create(String typeString){
		Class<? extends NotificationType> notificationTypeClass;
		try{
			notificationTypeClass = Class.forName(typeString).asSubclass(NotificationType.class);
		}catch(ClassNotFoundException | ClassCastException e){
			throw new IllegalArgumentException(typeString + " is not a NotificationType known", e);
		}
		return injector.getInstance(notificationTypeClass);
	}

}
