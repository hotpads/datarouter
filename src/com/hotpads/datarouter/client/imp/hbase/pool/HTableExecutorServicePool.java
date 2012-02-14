package com.hotpads.datarouter.client.imp.hbase.pool;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.ExecutorServiceTool;
import com.hotpads.util.core.concurrent.SemaphoreTool;
import com.hotpads.util.core.concurrent.ThreadTool;

public class HTableExecutorServicePool implements HTablePool{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected Long lastLoggedWarning = 0L;
	
	protected Configuration hBaseConfiguration;
	protected String clientName;
	protected Integer maxSize;
	
	//this turned out weird:
	// using Semaphore and BlockingDequeue here.  evolutionary complexity, but possibly more flexible
	protected Semaphore hTableSemaphore;
	protected BlockingDeque<HTableExecutorService> executorServiceQueue;
	protected Map<HTable,HTableExecutorService> activeHTables;
	
	protected Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName;
	
	
	public HTableExecutorServicePool(Configuration hBaseConfiguration, 
			String clientName, int maxSize,
			Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName){
		this.hBaseConfiguration = hBaseConfiguration;
		this.clientName = clientName;
		this.maxSize = maxSize;
		this.hTableSemaphore = new Semaphore(maxSize);
		this.executorServiceQueue = new LinkedBlockingDeque<HTableExecutorService>(maxSize);
		this.activeHTables = MapTool.createConcurrentHashMap();
		this.primaryKeyClassByName = primaryKeyClassByName;
	}
	
	
	@Override
	public HTable checkOut(String tableName){
		long checkoutRequestStartMs = System.currentTimeMillis();
		checkConsistencyAndAcquireSempahore(tableName);
		try{
			DRCounters.inc("connection getHTable "+tableName);
			HTableExecutorService hTableExecutorService = null;
			while(true){
				hTableExecutorService = executorServiceQueue.pollFirst();
				if(hTableExecutorService==null){
					hTableExecutorService = new HTableExecutorService();
					String counterName = "connection create HTable "+tableName;
					DRCounters.inc(counterName);
					logWithPoolInfo("created new HTable ThreadPool Executor for table", tableName);
					break;
				}
				if(!hTableExecutorService.isExpired()){
	//				logger.warn("connection got pooled HTable executor");
					DRCounters.inc("connection got pooled HTable executor");
					break;
				}
				//TODO background thread that actively discards expired pools
				logger.warn("discarded expired executorService"+getPoolInfoLogMessage());
				hTableExecutorService = null;//release it and loop around again
			}
		
			HTable hTable = null;
			HConnection hConnection = HConnectionManager.getConnection(hBaseConfiguration);
			hTable = new HTable(StringByteTool.getUtf8Bytes(tableName), hConnection, 
					hTableExecutorService.exec);
			activeHTables.put(hTable, hTableExecutorService);
			hTable.getWriteBuffer().clear();
			hTable.setAutoFlush(false);
			recordSlowCheckout(System.currentTimeMillis() - checkoutRequestStartMs);
			logIfInconsistentCounts(true, tableName);
			return hTable;
		}catch(IOException ioe){
			hTableSemaphore.release();//HTable didn't make it out into the wild, so we know it can't be checked in later
			throw new RuntimeException(ioe);
		}
	}
	
	
	@Override
	public void checkIn(HTable hTable, boolean possiblyTarnished){
		//do this first otherwise things may get hung up in the "active" map
		String tableName = StringByteTool.fromUtf8Bytes(hTable.getTableName());
		try{
			HTableExecutorService hTableExecutorService = activeHTables.remove(hTable);
			hTable.getWriteBuffer().clear();
			if(hTableExecutorService==null){
				logWithPoolInfo("HTable returned to pool but HTableExecutorService not found", tableName);
				DRCounters.inc("HTable returned to pool but HTableExecutorService not found");
				return;
			}
			hTableExecutorService.markLastCheckinMs();
			ThreadPoolExecutor exec = hTableExecutorService.exec;
			exec.purge();
			if(possiblyTarnished){//discard
				logWithPoolInfo("ThreadPoolExecutor possibly tarnished, discarding", tableName);
				DRCounters.inc("HTable executor possiblyTarnished "+tableName);	
				hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(hTableExecutorService.isDyingOrDead(tableName)){//discard
				logWithPoolInfo("ThreadPoolExecutor not reusable, discarding", tableName);
				DRCounters.inc("HTable executor isDyingOrDead "+tableName);	
				hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(!hTableExecutorService.isTaskQueueEmpty()){//discard
				logWithPoolInfo("ThreadPoolExecutor taskQueue not empty, discarding", tableName);
				DRCounters.inc("HTable executor taskQueue not empty "+tableName);	
				hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(!hTableExecutorService.waitForActiveThreadsToSettle(tableName)){//discard
				logWithPoolInfo("active thread count would not settle to 0", tableName);
				DRCounters.inc("HTable executor pool active threads won't quit "+tableName);	
				hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else{
				if(executorServiceQueue.offer(hTableExecutorService)){//keep it!
					DRCounters.inc("connection HTable returned to pool "+tableName);
				}else{//discard
					logWithPoolInfo("checkIn HTable but queue already full, so close and discard", tableName);
					DRCounters.inc("HTable executor pool overflow");	
					hTableExecutorService.terminateAndBlockUntilFinished(tableName);
				}
			}
		}finally{
			releaseSempahoreAndCheckConsistency(tableName);
		}
	}
	
	//for some reason, synchronizing this method wreaks and stops all progress
	protected /*synchronized*/ void checkConsistencyAndAcquireSempahore(String tableName){
		logIfInconsistentCounts(true, tableName);
		SemaphoreTool.acquire(hTableSemaphore);
	}
	
	protected synchronized void releaseSempahoreAndCheckConsistency(String tableName){
		hTableSemaphore.release();
		logIfInconsistentCounts(false, tableName);
	}
	
	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}
	
	public Integer getTotalPoolSize(){
		return executorServiceQueue.size();
	}
	
	/*
	 * From ThreadPoolExecutor javadoc:
	 * 
	 * <dd> A pool that is no longer referenced in a program <em>AND</em> has no
	 * remaining threads will be <tt>shutdown</tt> automatically. If you would
	 * like to ensure that unreferenced pools are reclaimed even if users forget
	 * to call {@link ThreadPoolExecutor#shutdown}, then you must arrange that
	 * unused threads eventually die, by setting appropriate keep-alive times,
	 * using a lower bound of zero core threads and/or setting {@link
	 * ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)}. </dd> </dl>
	 */
	public static class HTableExecutorService{
		protected Logger logger = Logger.getLogger(getClass());
		
		public static final Integer NUM_CORE_THREADS = 0;//see class comment regarding killing pools

		public static final Long TIMEOUT_MS = 15 * 1000L;//15 seconds
		
		protected ThreadPoolExecutor exec;
		protected Long createdMs;
		protected Long lastCheckinMs;
		
		public HTableExecutorService() {
			this.exec = new ThreadPoolExecutor(NUM_CORE_THREADS, Integer.MAX_VALUE,
			        60, TimeUnit.SECONDS,
			        new SynchronousQueue<Runnable>());
			this.exec.allowCoreThreadTimeOut(true);//see class comment regarding killing pools
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
				logger.warn("executor isShutdown, table:"+tableNameForLog);
				return true;
			}
			if(exec.isTerminated()){
				logger.warn("executor isTerminated, table:"+tableNameForLog);
				return true;
			}
			if(exec.isTerminating()){
				logger.warn("executor isTerminating, table:"+tableNameForLog);
				return true;
			}
			return false;//should be nice and clean for the next HTable
		}
		
		//probably don't need this method, but being safe while debugging
		public boolean waitForActiveThreadsToSettle(String tableNameForLog){
			if(exec.getActiveCount()==0){ return true; }
			ThreadTool.sleep(1);
			if(exec.getActiveCount()==0){
//				logger.warn("had to sleep a little to let threads finish, table:"+tableNameForLog);
				return true;
			}
			ThreadTool.sleep(10);
			if(exec.getActiveCount()==0){
				logger.warn("had to sleep a long time to let threads finish, table:"+tableNameForLog);
				return true;
			}
			logger.warn("still have active threads after 11ms, table:"+tableNameForLog);
			return false;
		}
		
		public void terminateAndBlockUntilFinished(String tableNameForLog){
			exec.shutdownNow();//should not block
			if(exec.getActiveCount()==0){ return; }
			//else we have issues... try to fix them
			exec.shutdownNow();
			if(exec.getActiveCount() > 0){
				logger.warn("getActiveCount() still > 0 after shutdownNow(), table:"+tableNameForLog);
			}
			ExecutorServiceTool.awaitTerminationForever(exec);//any better ideas?  alternative is memory leak
			logger.warn("awaitTermination finished!, table:"+tableNameForLog);
		}
	}
	
	
	/*********************** logging ************************************/

	protected void recordSlowCheckout(long checkOutDurationMs){
		if(checkOutDurationMs > 1){
			DRCounters.inc("connection open > 1ms on "+clientName);
			logger.warn("slow reserveConnection:"+checkOutDurationMs+"ms on "+clientName);
		}
	}

	static final int LEWAY = 0;
	
	protected void logIfInconsistentCounts(boolean checkOut, String tableName){
		int semaphoreAvailable = hTableSemaphorePermitsRemaining();
		int numActiveHTables = activeHTables.size();
		if(checkOut){ ++numActiveHTables; }//because the table is not added to the active table map until after this method is called
		int diff = maxSize - semaphoreAvailable - numActiveHTables;
		if(diff > LEWAY){
			logWithPoolInfo("inconsistent pool counts on "+(checkOut?"checkOut":"checkIn"), tableName);
		}
	}
	
	protected void logWithPoolInfo(String message, String tableName){
		logger.warn(getPoolInfoLogMessage()+", "+message);
	}
	
	protected String getPoolInfoLogMessage(){
		return "clientName="+clientName
				+", HTables[max="+maxSize
				+", active="+activeHTables.size()
				+", available="+hTableSemaphorePermitsRemaining()
				+", waiting="+hTableSemaphore.getQueueLength()+"]"
				+", ExecServices[idle="+executorServiceQueue.size()+"]";
	}
	
	protected int hTableSemaphorePermitsRemaining(){
		return hTableSemaphore.availablePermits();//seems to always be 1 lower?
	}
	
}
