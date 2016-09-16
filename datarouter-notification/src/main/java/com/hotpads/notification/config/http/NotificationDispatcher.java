package com.hotpads.notification.config.http;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.notification.NotificationTesterHandler;
import com.hotpads.notification.alias.NotificationAliasHandler;

public class NotificationDispatcher extends BaseDispatcher{

	public static final String URL_DATAROUTER = DatarouterWebDispatcher.PATH_datarouter;

	public static final String
			NOTIFICATION_ALIAS = "/notification/alias",
			NOTIFICATION_TESTER = "/notification/tester";

	public NotificationDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, URL_DATAROUTER);

		handleDir(URL_DATAROUTER + NOTIFICATION_ALIAS).withHandler(NotificationAliasHandler.class);
		handleDir(URL_DATAROUTER + NOTIFICATION_TESTER).withHandler(NotificationTesterHandler.class);
	}

}
