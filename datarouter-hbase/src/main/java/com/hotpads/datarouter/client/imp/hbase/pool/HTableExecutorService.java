package com.hotpads.datarouter.client.imp.hbase.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.core.concurrent.ExecutorServiceTool;
import com.hotpads.util.core.concurrent.ThreadTool;

/*
 * The HBase client uses one thread per regionserver per HTable.  This wrapper class stores a java ExecSvc with 1
 * thread per regionserver.  It can be reused across different tables.
 */
public class HTableExecutorService{
	private static final Logger logger = LoggerFactory.getLogger(HTableExecutorService.class);

	private static final long TIMEOUT_MS = 10 * 1000L;

	//final fields
	private final ThreadPoolExecutor exec;
	private final long createdMs;
	
	private volatile long lastCheckinMs;

	public HTableExecutorService(int minThreads, int maxThreads){
		//it's important to use a bounded queue as the executor service won't grow past minThreads until you fill the
		// queue.  SynchronousQueue is a special zero size queue that will cause the exec svc to grow immediately
		BlockingQueue<Runnable> queue = new SynchronousQueue<>();
		//having more regionservers than maxThreads will cause a RejectedExecutionException that will kill your hbase 
		// request, so provide a high maxThreads
		this.exec = new ThreadPoolExecutor(minThreads, maxThreads, 60, TimeUnit.SECONDS, queue);
		this.exec.allowCoreThreadTimeOut(true);// see class comment regarding killing pools
		this.createdMs = System.currentTimeMillis();
		this.lastCheckinMs = createdMs;
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
		if(exec.getActiveCount() == 0){
			return true;
		}
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
		if(exec.getActiveCount() == 0){
			return;
		}
		// else we have issues... try to fix them
		exec.shutdownNow();
		if(exec.getActiveCount() > 0){
			logger.warn("getActiveCount() still > 0 after shutdownNow(), table:" + tableNameForLog, new Exception());
		}
		ExecutorServiceTool.awaitTerminationForever(exec);// any better ideas? alternative is memory leak
		logger.warn("awaitTermination finished!, table:" + tableNameForLog);
	}
	
	public ThreadPoolExecutor getExec(){
		return exec;
	}
}
