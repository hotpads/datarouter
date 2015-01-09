package com.hotpads.datarouter.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.util.NullProvider;
import com.hotpads.setting.DatarouterSettings;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public class DatarouterTestGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(ExecutorService.class).toProvider(DatarouterExecutorServiceProvider.class);
		bind(DatarouterSettings.class).toProvider(NullProvider.create(DatarouterSettings.class));
	}

	
	
	@Singleton
	public static class DatarouterExecutorServiceProvider implements Provider<ExecutorService>{
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
	
}
