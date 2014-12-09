package com.hotpads;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hotpads.logging.LoggingConfigLoader;

public abstract class DataRouterLoader implements ServletContextListener{

	private static DataRouterLoader instance;

	public static DataRouterLoader getDataRouterLoader(){
		return instance;
	}

	private List<HotPadsWebAppListener> listeners;

	protected abstract void init(ServletContext servletContext);

	public DataRouterLoader(){
		instance = this;
	}

	@Override
	public void contextInitialized(ServletContextEvent event){
		listeners = new LinkedList<>();
		init(event.getServletContext());
		for(Class<? extends HotPadsWebAppListener> listenerClass : getListenerClasses()){
			HotPadsWebAppListener listener = getInstance(listenerClass);
			listeners.add(listener);
			listener.setServletContext(event.getServletContext());
			listener.onStartUp();
		}
	}

	public abstract <T>T getInstance(Class<? extends T> clazz);

	private List<Class<? extends HotPadsWebAppListener>> getListenerClasses(){
		List<Class<? extends HotPadsWebAppListener>> classes = new LinkedList<>();
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
