package com.hotpads.datarouter.client.imp.hbase.factory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.DynamicClientFactory;
import com.hotpads.datarouter.routing.DataRouter;

public class HBaseDynamicClientFactory 
extends HBaseSimpleClientFactory
implements DynamicClientFactory{

	protected int numKeepAliveFailures;
	protected ScheduledExecutorService keepAliveExecutor;

	public HBaseDynamicClientFactory(
			DataRouter router, String clientName, 
			String configFileLocation){
		super(router, clientName, configFileLocation);
		this.keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
		this.keepAliveExecutor.scheduleWithFixedDelay(
				new HBaseClientKeepAliveTester(), 0, KEEP_ALIVE_TEST_PERIOD_MS, TimeUnit.MILLISECONDS); 
	}
	
	
	/******************************** keepAlive tests **********************************/

	@Override
	public boolean shouldReconnect(){
	    return numKeepAliveFailures >= RECONNECT_AFTER_X_FAILURES;
	}


	protected class HBaseClientKeepAliveTester implements Runnable{
		Logger logger = Logger.getLogger(getClass());

		@Override
		public void run(){
			Thread.currentThread().setName("DataRouter client keepAliveTest:"+clientName);
			try{
				if(client!=null){
					//?? this seems to succeed even when i shut down hbase ??
					HBaseAdmin.checkHBaseAvailable(hbConfig);
				}
				logger.warn(clientName+" pass");
				numKeepAliveFailures = 0;
			}catch(Exception e){
				logger.warn(clientName+" fail");
				++numKeepAliveFailures;
			}
		}
	}
	
	
}
