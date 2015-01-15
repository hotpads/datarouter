package com.hotpads.datarouter.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.hotpads.DatarouterInjector;
import com.hotpads.GuiceInjector;
import com.hotpads.datarouter.config.DatarouterGuiceModule.DatarouterExecutorServiceProvider.DatarouterExecutorService;
import com.hotpads.datarouter.util.ApplicationPaths;
import com.hotpads.datarouter.util.GuiceApplicationPaths;
import com.hotpads.util.core.concurrent.NamedThreadFactory;
import com.hotpads.util.http.json.GsonJsonSerializer;
import com.hotpads.util.http.json.JsonSerializer;

public class DatarouterGuiceModule extends ServletModule{

	@Override
	protected void configureServlets(){
		bind(ApplicationPaths.class).to(GuiceApplicationPaths.class).in(Scopes.SINGLETON);
		bind(ExecutorService.class).annotatedWith(DatarouterExecutorService.class).toProvider(
				DatarouterExecutorServiceProvider.class).in(Scopes.SINGLETON);
		bind(DatarouterInjector.class).to(GuiceInjector.class);
		bind(JsonSerializer.class).annotatedWith(HandlerDefaultSerializer.class).to(GsonJsonSerializer.class);
	}

	

	//unused: causing guice startup errors.  not sure why
	@Singleton
	public static class DatarouterExecutorServiceProvider implements Provider<ExecutorService>{

		@BindingAnnotation 
		@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) 
		@Retention(RetentionPolicy.RUNTIME)
		public @interface DatarouterExecutorService {}
		
		private ExecutorService executorService;
		
		public DatarouterExecutorServiceProvider(){
			int id = System.identityHashCode(this);
			ThreadGroup threadGroup = new ThreadGroup("Datarouter-ThreadGroup-"+id);
			ThreadFactory threadFactory = new NamedThreadFactory(threadGroup, "Datarouter-ThreadFactory-"+id, true);
			this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
		            new SynchronousQueue<Runnable>(), threadFactory);
		}
		@Override
		public ExecutorService get(){
			return executorService;
		}
	}
	
	@BindingAnnotation 
	@Target({ ElementType.FIELD, ElementType.PARAMETER }) 
	@Retention(RetentionPolicy.RUNTIME)
	public @interface HandlerDefaultSerializer{}
	
}
