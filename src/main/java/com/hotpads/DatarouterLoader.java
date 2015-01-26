package com.hotpads;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hotpads.handler.DatarouterContextLoader;
import com.hotpads.logging.LoggingConfigLoader;

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
		return classes;
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		for(HotPadsWebAppListener listener : listeners){
			listener.onShutDown();
		}
		listeners.clear();
		listeners = null;
	}

}
