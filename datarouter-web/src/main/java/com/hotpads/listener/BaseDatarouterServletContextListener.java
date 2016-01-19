package com.hotpads.listener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.inject.InjectorRetriever;
import com.hotpads.handler.DatarouterShutdownAppListener;
import com.hotpads.handler.TomcatWebAppNamesWebAppListener;
import com.hotpads.job.trigger.JobSchedulerAppListener;
import com.hotpads.logging.LoggingAppListener;

/**
 * Use this to configure ServletContextListeners in Java rather than web.xml.  Listeners should implement the
 * DatarouterWebAppListener interface and be added to the getWebAppListeners() method
 */
public abstract class BaseDatarouterServletContextListener implements ServletContextListener, InjectorRetriever{

	private List<DatarouterAppListener> listeners;

	@Override
	public void contextInitialized(ServletContextEvent event){
		DatarouterInjector injector = getInjector(event.getServletContext());

		listeners = new LinkedList<>();

		for(Class<? extends DatarouterAppListener> listenerClass : getAppListeners()){
			DatarouterAppListener listener = injector.getInstance(listenerClass);
			listeners.add(listener);
			listener.onStartUp();
		}

		for(Class<? extends DatarouterWebAppListener> listenerClass : getWebAppListeners()){
			DatarouterWebAppListener listener = injector.getInstance(listenerClass);
			listeners.add(listener);
			listener.setServletContext(event.getServletContext());
			listener.onStartUp();
		}
	}

	protected List<Class<? extends DatarouterAppListener>> getAppListeners(){
		List<Class<? extends DatarouterAppListener>> classes = new LinkedList<>();
		classes.add(DatarouterShutdownAppListener.class);
		classes.add(LoggingAppListener.class);
		classes.add(JobSchedulerAppListener.class);
		classes.add(ExecutorsAppListener.class);
		classes.add(HttpClientAppListener.class);
		return classes;
	}

	protected List<Class<? extends DatarouterWebAppListener>> getWebAppListeners(){
		List<Class<? extends DatarouterWebAppListener>> classes = new LinkedList<>();
		classes.add(TomcatWebAppNamesWebAppListener.class);
		return classes;
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		Collections.reverse(listeners);
		for(DatarouterAppListener listener : listeners){
			listener.onShutDown();
		}
		listeners.clear();
		listeners = null;
	}

}
