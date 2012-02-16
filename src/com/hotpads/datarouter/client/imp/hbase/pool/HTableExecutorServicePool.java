package com.hotpads.datarouter.client.imp.hbase.pool;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.SemaphoreTool;

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
					logWithPoolInfo("created new HTableExecutorService", tableName);
					break;
				}
				if( ! hTableExecutorService.isExpired()){
	//				logger.warn("connection got pooled HTable executor");
					DRCounters.inc("got pooled HTable executor");
					break;//done.  we got an unexpired one, exit the while loop
				}
				
				//If we get here we're draining the queue of expired ExecutorServices.  We could do this
				// in a background thread, but this should eventually accomplish the same thing 
				// with fewer moving parts.  Goal is to be able to allow large queue sizes for bursts
				// but to free up the memory of hundreds or thousands of threads in quiet times when
				// other things might be bursting
				logWithPoolInfo("discarded expired HTableExecutorService", tableName);
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
	
	/*********************** logging ************************************/

	protected void recordSlowCheckout(long checkOutDurationMs){
		if(checkOutDurationMs > 1){
			DRCounters.inc("connection open > 1ms on "+clientName);
			logger.warn("slow reserveConnection:"+checkOutDurationMs+"ms on "+clientName);
		}
	}

	static final int LEWAY = 0;
	static final long THROTTLE_INCONSISTENT_LOG_EVERY_X_MS = 500;
	
	protected void logIfInconsistentCounts(boolean checkOut, String tableName){
		long msSinceLastLog = System.currentTimeMillis() - lastLoggedWarning;
		if(msSinceLastLog < THROTTLE_INCONSISTENT_LOG_EVERY_X_MS){ return; }
		int semaphoreAvailable = hTableSemaphorePermitsRemaining();
		int numActiveHTables = activeHTables.size();
		if(checkOut){ ++numActiveHTables; }//because the table is not added to the active table map until after this method is called
		int diff = maxSize - semaphoreAvailable - numActiveHTables;
		if(diff > LEWAY){
			logWithPoolInfo("inconsistent pool counts on "+(checkOut?"checkOut":"checkIn"), tableName);
		}
		lastLoggedWarning = System.currentTimeMillis();
	}
	
	protected void logWithPoolInfo(String message, String tableName){
		logger.warn(getPoolInfoLogMessage(tableName)+", "+message);
	}
	
	protected String getPoolInfoLogMessage(String tableName){
		return "HTables[max="+maxSize
				+", active="+activeHTables.size()
				+", available="+hTableSemaphorePermitsRemaining()
				+", waiting="+hTableSemaphore.getQueueLength()+"]"
				+", ExecServices[idle="+executorServiceQueue.size()+"]"
				+", client="+clientName
				+", table="+tableName;
	}
	
	protected int hTableSemaphorePermitsRemaining(){
		return hTableSemaphore.availablePermits();//seems to always be 1 lower?
	}
	
}
