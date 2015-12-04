package com.hotpads.example.config.guice;

import com.google.inject.servlet.ServletModule;
import com.hotpads.example.config.http.ExampleDispatcherServlet;

public class ExampleWebGuiceModule extends ServletModule{

	@Override
	protected void configureServlets(){
		serve("/*").with(ExampleDispatcherServlet.class);
	}

}
