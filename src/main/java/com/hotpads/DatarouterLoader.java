package com.hotpads;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hotpads.handler.DatarouterContextLoader;
import com.hotpads.handler.LocalWebAppNamesLoader;
import com.hotpads.job.trigger.JobSchedulerLoader;
import com.hotpads.logging.LoggingConfigLoader;
import com.hotpads.util.core.concurrent.FutureTool;

public abstract class DatarouterLoader implements ServletContextListener{

	private List<HotPadsWebAppListener> listeners;

	protected abstract void init(ServletContext servletContext);

	protected abstract DatarouterInjector getInjector();

	@Override
	public void contextInitialized(ServletContextEvent event){
		init(event.getServletContext());

		listeners = new LinkedList<>();
		for(Class<? extends HotPadsWebAppListener> listenerClass : getListenerClasses()){
			HotPadsWebAppListener listener = getInjector().getInstance(listenerClass);
			listeners.add(listener);
			listener.setServletContext(event.getServletContext());
			listener.onStartUp();
		}
	}

	protected List<Class<? extends HotPadsWebAppListener>> getListenerClasses(){
		List<Class<? extends HotPadsWebAppListener>> classes = new LinkedList<>();
		classes.add(DatarouterContextLoader.class);
		classes.add(LoggingConfigLoader.class);
		classes.add(LocalWebAppNamesLoader.class);
		classes.add(JobSchedulerLoader.class);
		return classes;
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		for(HotPadsWebAppListener listener : listeners){
			listener.onShutDown();
		}
		listeners.clear();
		listeners = null;
		
		for(ExecutorService executor : getInjector().getInstancesOfType(ExecutorService.class)){
			FutureTool.finishAndShutdown(executor, 5L, TimeUnit.SECONDS);
		}
	}

}
