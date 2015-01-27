package com.hotpads.guice;

import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.hotpads.DatarouterInjector;
import com.hotpads.DatarouterLoader;
import com.hotpads.util.core.java.ReflectionTool;

public class GuiceDatarouterLoader extends DatarouterLoader{

	private Injector injector;

	@Override
	protected void init(ServletContext servletContext){
		this.injector = (Injector)servletContext.getAttribute(Injector.class.getName());
	}

	@Override
	protected DatarouterInjector getInjector(){
		return injector.getInstance(DatarouterInjector.class);
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event){
		super.contextDestroyed(event);
		
		for(Entry<Key<?>, Binding<?>> bindingEntry : injector.getAllBindings().entrySet()){
			Class<?> bindedType = bindingEntry.getKey().getTypeLiteral().getRawType();
			if (ReflectionTool.getAllSuperClassesAndInterfaces(bindedType).contains(ExecutorService.class)){
				ExecutorService executor = (ExecutorService) bindingEntry.getValue().getProvider().get();
				executor.shutdownNow();
			}
		}
	}

}
