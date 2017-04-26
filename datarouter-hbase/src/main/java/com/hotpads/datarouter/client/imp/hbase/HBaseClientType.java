package com.hotpads.datarouter.client.imp.hbase;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.DefaultClientTypes;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClientFactory;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.util.core.lang.ClassTool;

@Singleton
public class HBaseClientType extends BaseHBaseClientType{

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
		return DefaultClientTypes.CLIENT_TYPE_hbase;
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
		private DatarouterInjector injector;

		@Test
		public void testClassLocation(){
			String actualClassName = HBaseClientType.class.getCanonicalName();
			Assert.assertEquals(DefaultClientTypes.CLIENT_CLASS_hbase, actualClassName);
			injector.getInstance(ClassTool.forName(DefaultClientTypes.CLIENT_CLASS_hbase));
		}
	}
}
