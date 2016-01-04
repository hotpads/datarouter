package com.hotpads.datarouter.test;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.availability.TestClientAvailabilitySettings;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.util.NullProvider;

public class DatarouterStorageTestGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(DatarouterSettings.class).toProvider(NullProvider.create(DatarouterSettings.class));
		bind(ClientAvailabilitySettings.class).to(TestClientAvailabilitySettings.class);
	}

}
