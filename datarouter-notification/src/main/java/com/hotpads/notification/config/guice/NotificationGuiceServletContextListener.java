package com.hotpads.notification.config.guice;

import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

public class NotificationGuiceServletContextListener extends GuiceServletContextListener{

	@Override
	protected Injector getInjector(){
		return new NotificationInjectorProvider(Stage.DEVELOPMENT).get();
	}

}
