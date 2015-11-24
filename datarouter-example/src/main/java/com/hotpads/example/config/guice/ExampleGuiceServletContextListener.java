package com.hotpads.example.config.guice;

import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

public class ExampleGuiceServletContextListener extends GuiceServletContextListener{

	@Override
	protected Injector getInjector(){
		return new ExampleInjectorProvider(Stage.DEVELOPMENT).get();
	}

}
