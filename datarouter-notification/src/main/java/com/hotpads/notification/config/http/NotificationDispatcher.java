package com.hotpads.notification.config.http;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.notification.SystemApiHandler;
import com.hotpads.notification.handler.DatarouterNotificationDefaultHandler;

public class NotificationDispatcher extends BaseDispatcher{

	public NotificationDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, "");
		handleDir("/system").withHandler(SystemApiHandler.class);
		handle("|/").withHandler(DatarouterNotificationDefaultHandler.class);
	}

}
