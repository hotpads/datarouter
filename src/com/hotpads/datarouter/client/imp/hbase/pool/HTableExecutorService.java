package com.hotpads.datarouter.client.imp.hbase.pool;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hotpads.util.core.concurrent.ExecutorServiceTool;
import com.hotpads.util.core.concurrent.ThreadTool;

public class HTableExecutorService{
	protected Logger logger = Logger.getLogger(getClass());

	public static final Integer NUM_CORE_THREADS = 0;// see class comment regarding killing pools

	public static final Long TIMEOUT_MS = 60 * 1000L;// 60 seconds

	protected ThreadPoolExecutor exec;
	protected Long createdMs;
	protected Long lastCheckinMs;

	public HTableExecutorService(){
		this.exec = new ThreadPoolExecutor(NUM_CORE_THREADS, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
						new SynchronousQueue<Runnable>());
		this.exec.allowCoreThreadTimeOut(true);// see class comment regarding killing pools
		this.createdMs = System.currentTimeMillis();
		this.lastCheckinMs = this.createdMs;
	}

	public void markLastCheckinMs(){
		lastCheckinMs = System.currentTimeMillis();
	}

	public boolean isExpired(){
		long elapsedMs = System.currentTimeMillis() - lastCheckinMs;
		return elapsedMs > TIMEOUT_MS;
	}

	public void purge(){
		exec.purge();
	}

	public boolean isTaskQueueEmpty(){
		return exec.getQueue().size() == 0;
	}

	public boolean isDyingOrDead(String tableNameForLog){
		if(exec.isShutdown()){
			logger.warn("executor isShutdown, table:" + tableNameForLog);
			return true;
		}
		if(exec.isTerminated()){
			logger.warn("executor isTerminated, table:" + tableNameForLog);
			return true;
		}
		if(exec.isTerminating()){
			logger.warn("executor isTerminating, table:" + tableNameForLog);
			return true;
		}
		return false;// should be nice and clean for the next HTable
	}

	// probably don't need this method, but being safe while debugging
	public boolean waitForActiveThreadsToSettle(String tableNameForLog){
		if(exec.getActiveCount() == 0){ return true; }
		ThreadTool.sleep(1);
		if(exec.getActiveCount() == 0){
			// logger.warn("had to sleep a little to let threads finish, table:"+tableNameForLog);
			return true;
		}
		ThreadTool.sleep(10);
		if(exec.getActiveCount() == 0){
			logger.warn("had to sleep a long time to let threads finish, table:" + tableNameForLog);
			return true;
		}
		logger.warn("still have active threads after 11ms, table:" + tableNameForLog);
		return false;
	}

	public void terminateAndBlockUntilFinished(String tableNameForLog){
		exec.shutdownNow();// should not block
		if(exec.getActiveCount() == 0){ return; }
		// else we have issues... try to fix them
		exec.shutdownNow();
		if(exec.getActiveCount() > 0){
			logger.warn("getActiveCount() still > 0 after shutdownNow(), table:" + tableNameForLog);
		}
		ExecutorServiceTool.awaitTerminationForever(exec);// any better ideas? alternative is memory leak
		logger.warn("awaitTermination finished!, table:" + tableNameForLog);
	}
}