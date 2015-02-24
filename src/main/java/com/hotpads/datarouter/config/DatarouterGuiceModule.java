package com.hotpads.datarouter.config;

import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.hotpads.DatarouterInjector;
import com.hotpads.GuiceInjector;
import com.hotpads.datarouter.util.ApplicationPaths;
import com.hotpads.datarouter.util.GuiceApplicationPaths;
import com.hotpads.guice.DatarouterExecutorGuiceModule;
import com.hotpads.job.trigger.EmptyTriggerGroup;
import com.hotpads.job.trigger.TriggerGroup;
import com.hotpads.util.http.json.GsonJsonSerializer;
import com.hotpads.util.http.json.JsonSerializer;

public class DatarouterGuiceModule extends ServletModule{

	public static final String DEFAULT_HANDLER_SERIALIZER = "defaultHandlerSerializer";
	
	private Class<? extends TriggerGroup> triggerGroupClass = EmptyTriggerGroup.class;

	@Override
	protected void configureServlets(){
		bind(ApplicationPaths.class).to(GuiceApplicationPaths.class).in(Scopes.SINGLETON);
		bind(DatarouterInjector.class).to(GuiceInjector.class);
		bind(JsonSerializer.class).annotatedWith(Names.named(DEFAULT_HANDLER_SERIALIZER)).to(GsonJsonSerializer.class);
		bind(TriggerGroup.class).to(triggerGroupClass);
		
		install(new DatarouterExecutorGuiceModule());
	}

	public <T extends TriggerGroup> Module withTriggerGroup(Class<T> triggerGroupClass){
		this.triggerGroupClass = triggerGroupClass;
		return this;
	}

}
