package com.hotpads.datarouter.client.imp.hbase.factory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.client.HConnection;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.DynamicClientFactory;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseDynamicClientFactory 
extends HBaseSimpleClientFactory
implements DynamicClientFactory{
	
	public static final Long KEEP_ALIVE_TEST_PERIOD_MS = 10*1000L;
	public static final Integer RECONNECT_AFTER_X_FAILURES = 3;
	public static final Long ESTIMATED_DOWNTIME = KEEP_ALIVE_TEST_PERIOD_MS * RECONNECT_AFTER_X_FAILURES;
	static final Long MIN_MS_BETWEEN_RECONNECTS = 10000L;

	protected int numKeepAliveFailures;
	protected ScheduledExecutorService keepAliveExecutor;
	protected Long lastReconnectTimeMs = System.currentTimeMillis();

	public HBaseDynamicClientFactory(
			DataRouterContext drContext,
			String clientName, 
			ExecutorService executorService){
		super(drContext, clientName, executorService);
		this.keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
		this.keepAliveExecutor.scheduleWithFixedDelay(
				new HBaseClientKeepAliveTester(), 0, KEEP_ALIVE_TEST_PERIOD_MS, TimeUnit.MILLISECONDS); 
		if(ESTIMATED_DOWNTIME < CREATE_CLIENT_TIMEOUT_MS){
			logger.warn("ESTIMATED_DOWNTIME("+ESTIMATED_DOWNTIME+"ms) < CREATE_CLIENT_TIMEOUT_MS("
					+CREATE_CLIENT_TIMEOUT_MS+"ms)");
		}
	}
	
	
	/******************************** keepAlive tests **********************************/

	protected class HBaseClientKeepAliveTester implements Runnable{
		Logger logger = Logger.getLogger(getClass());
		
		public HBaseClientKeepAliveTester() {
		}

		@Override
		public void run(){
			try{//don't let this thread die
				if(client!=null){
					Thread.currentThread().setName("DataRouter client keepAliveTest:"+clientName);
					PhaseTimer timer = new PhaseTimer("keepAliveCheck for HBaseClient "+clientName);
					timer.add("hTablePoolSize:"+client.getTotalPoolSize());
					try{
						//?? this seems to succeed even when i shut down hbase ??
						//HBaseAdmin.checkHBaseAvailable(hBaseConfig);//leaves 2 new daemon threads running after every call
						
						boolean keepAliveTestTableAvailable = hBaseAdmin.isTableAvailable(KeepAlive.TABLE_NAME);
						if(!keepAliveTestTableAvailable){
							throw new DataAccessException("can't reach table:"+KeepAlive.TABLE_NAME+".  create with command:"
									+"create '"+KeepAlive.TABLE_NAME+"', {NAME=>'a',VERSIONS=>1}");
						}
						
						
						timer.add("passed");
		//				logger.warn(clientName+" pass");
						numKeepAliveFailures = 0;
	//				}catch(MasterNotRunningException e){
	//					timer.add(e.getClass().getSimpleName());
	//					++numKeepAliveFailures;
	//				}catch(ZooKeeperConnectionException e){
	//					timer.add(e.getClass().getSimpleName());
	//					++numKeepAliveFailures;
					}catch(Exception e){
						logger.error(e);
						timer.add("Exception");
						++numKeepAliveFailures;
						logger.warn(timer);
					}
//					logger.warn(timer);//comment in if you want more log spam
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
					if(hBaseAdmin!=null){
						HConnection hConnection = hBaseAdmin.getConnection();
						if(hConnection!=null){
							hConnection.close();//this may be a bad idea because the connection is used by other clients
						}
						hBaseAdmin.close();
					}
					hBaseAdmin = null;
					client = null;//just set it to null and the next read will try to create a new one
					numKeepAliveFailures = 0;
	//				client.shutdown();
				}
			}catch(Exception e){
				logger.error(e);
			}
		}
	}


	@Override
	public boolean shouldReconnect(){
	    boolean manyFailures = numKeepAliveFailures >= RECONNECT_AFTER_X_FAILURES;
	    return manyFailures;
	}
	
}
