package com.hotpads.datarouter.config;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.client.availability.ClientAvailabilityClusterSettings;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.inject.guice.GuiceInjector;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;

public class DatarouterStorageGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(DatarouterInjector.class).to(GuiceInjector.class);
		bind(ClientAvailabilitySettings.class).to(ClientAvailabilityClusterSettings.class);
		install(new DatarouterExecutorGuiceModule());
	}

}
