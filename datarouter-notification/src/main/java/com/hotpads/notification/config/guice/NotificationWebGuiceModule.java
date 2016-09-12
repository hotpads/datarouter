package com.hotpads.notification.config.guice;

import com.google.inject.servlet.ServletModule;
import com.hotpads.datarouter.config.staticfiles.StaticFileFilter;
import com.hotpads.notification.config.http.NotificationDispatcherServlet;

public class NotificationWebGuiceModule extends ServletModule{

	@Override
	protected void configureServlets(){
		filter("/*").through(StaticFileFilter.class);

		serve("/*").with(NotificationDispatcherServlet.class);
	}

}
