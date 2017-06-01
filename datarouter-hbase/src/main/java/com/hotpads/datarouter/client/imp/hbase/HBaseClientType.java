package com.hotpads.datarouter.client.imp.hbase;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientTypeRegistry;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClientFactory;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;

@Singleton
public class HBaseClientType extends BaseHBaseClientType{

	public static final String NAME = "hbase";

	private final ClientAvailabilitySettings clientAvailabilitySettings;
	private final ExecutorService executor;

	@Inject
	public HBaseClientType(ClientAvailabilitySettings clientAvailabilitySettings,
			@Named(DatarouterExecutorGuiceModule.POOL_hbaseClientExecutor) ExecutorService executor){
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.executor = executor;
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DatarouterProperties datarouterProperties, Datarouter datarouter,
			String clientName){
		return new HBaseClientFactory(datarouterProperties, datarouter, clientName, clientAvailabilitySettings,
				executor, this);
	}

	/********************** tests ****************************/

	@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
	public static class HBaseClientTypeTests{
		@Inject
		private ClientTypeRegistry clientTypeRegistry;

		@Test
		public void testClassLocation(){
			Assert.assertEquals(clientTypeRegistry.create(NAME).getClass(), HBaseClientType.class);
		}
	}
}
