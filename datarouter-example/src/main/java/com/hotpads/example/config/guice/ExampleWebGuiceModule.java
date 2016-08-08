package com.hotpads.example.config.guice;

import com.google.inject.servlet.ServletModule;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.config.staticfiles.StaticFileFilter;
import com.hotpads.example.config.ExampleDatarouterProperties;
import com.hotpads.example.config.http.ExampleDispatcherServlet;

public class ExampleWebGuiceModule extends ServletModule{

	@Override
	protected void configureServlets(){
		filter("/*").through(StaticFileFilter.class);

		serve("/*").with(ExampleDispatcherServlet.class);

		bind(DatarouterProperties.class).to(ExampleDatarouterProperties.class);
	}

}
