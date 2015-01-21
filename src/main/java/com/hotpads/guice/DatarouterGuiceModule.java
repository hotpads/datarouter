package com.hotpads.guice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.hotpads.DatarouterInjector;
import com.hotpads.GuiceInjector;
import com.hotpads.datarouter.util.ApplicationPaths;
import com.hotpads.datarouter.util.GuiceApplicationPaths;
import com.hotpads.util.http.json.GsonJsonSerializer;
import com.hotpads.util.http.json.JsonSerializer;

public class DatarouterGuiceModule extends ServletModule{

	@Override
	protected void configureServlets(){
		bind(ApplicationPaths.class).to(GuiceApplicationPaths.class).in(Scopes.SINGLETON);
		bind(DatarouterInjector.class).to(GuiceInjector.class);
		bind(JsonSerializer.class).annotatedWith(HandlerDefaultSerializer.class).to(GsonJsonSerializer.class);
		
		install(new DatarouterExecutorGuiceModule());
	}
	
	@BindingAnnotation 
	@Target({ ElementType.FIELD, ElementType.PARAMETER }) 
	@Retention(RetentionPolicy.RUNTIME)
	public @interface HandlerDefaultSerializer{}
	
}
