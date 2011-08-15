package com.hotpads.datarouter.client.imp.hbase.factory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.DynamicClientFactory;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseDynamicClientFactory 
extends HBaseSimpleClientFactory
implements DynamicClientFactory{
	
	public static final Long KEEP_ALIVE_TEST_PERIOD_MS = 10*1000L;
	public static final Integer RECONNECT_AFTER_X_FAILURES = 2;
	static final Long MIN_MS_BETWEEN_RECONNECTS = 10000L;

	protected int numKeepAliveFailures;
	protected ScheduledExecutorService keepAliveExecutor;
	protected Long lastReconnectTimeMs = System.currentTimeMillis();

	public HBaseDynamicClientFactory(
			DataRouter router, String clientName, 
			String configFileLocation, 
			ExecutorService executorService){
		super(router, clientName, configFileLocation, executorService);
		this.keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
		this.keepAliveExecutor.scheduleWithFixedDelay(
				new HBaseClientKeepAliveTester(), 0, KEEP_ALIVE_TEST_PERIOD_MS, TimeUnit.MILLISECONDS); 
	}
	
	
	/******************************** keepAlive tests **********************************/

	@Override
	public boolean shouldReconnect(){
	    boolean manyFailures = numKeepAliveFailures >= RECONNECT_AFTER_X_FAILURES;
	    return manyFailures;
	}


	protected class HBaseClientKeepAliveTester implements Runnable{
		Logger logger = Logger.getLogger(getClass());
		
		public HBaseClientKeepAliveTester() {
		}

		@Override
		public void run(){
			if(client!=null){
				Thread.currentThread().setName("DataRouter client keepAliveTest:"+clientName);
				PhaseTimer timer = new PhaseTimer("keepAliveCheck for HBaseClient "+clientName);
				timer.add("hTablePoolSize:"+client.getTotalPoolSize());
				try{
					hBaseAdmin.isMasterRunning();
					//?? this seems to succeed even when i shut down hbase ??
//					HBaseAdmin.checkHBaseAvailable(hbConfig);//leaves 2 new daemon threads running after every call
					timer.add("passed");
	//				logger.warn(clientName+" pass");
					numKeepAliveFailures = 0;
				}catch(MasterNotRunningException e){
					timer.add("MasterNotRunningException");
					++numKeepAliveFailures;
				}catch(ZooKeeperConnectionException e){
					timer.add("ZooKeeperConnectionException");
					++numKeepAliveFailures;
				}
				logger.warn(timer);
			}
			
			if(numKeepAliveFailures > 0){
				hBaseAdmin = null;
			}
			
			if(shouldReconnect()){
				long msSinceLastReconnect = System.currentTimeMillis() - lastReconnectTimeMs;
				long sleepFor = MIN_MS_BETWEEN_RECONNECTS - msSinceLastReconnect;
				if(sleepFor < 1){ sleepFor = 1; }
				logger.warn("sleeping "+sleepFor+"ms before reconnecting");
				try {
					Thread.sleep(sleepFor);
				} catch (InterruptedException e) {
				}
				lastReconnectTimeMs = System.currentTimeMillis();

				logger.warn("setting client "+clientName+"=null");
				client = null;//just set it to null and the next read will try to create a new one
				numKeepAliveFailures = 0;
//				client.shutdown();
			}
		}
	}
	
	
}
