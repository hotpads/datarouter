package com.hotpads.datarouter.client.imp.hbase.pool;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.SemaphoreTool;
import com.hotpads.util.datastructs.MutableString;

public class HTableExecutorServicePool implements HTablePool{
	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected static Boolean LOG_ACTIONS = true;
	private static final long LOG_SEMAPHORE_ACQUISITIONS_OVER_MS = 2000L;

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
		this.activeHTables = DrMapTool.createConcurrentHashMap();
		this.primaryKeyClassByName = primaryKeyClassByName;
	}


	@Override
	public HTable checkOut(String tableName, MutableString progress){
		long checkoutRequestStartMs = System.currentTimeMillis();
		checkConsistencyAndAcquireSempahore(tableName);
		setProgress(progress, "passed semaphore");
		HTableExecutorService hTableExecutorService = null;
		HTable hTable = null;
		try{
			DRCounters.incClientTable(HBaseClientType.INSTANCE, "connection getHTable", clientName, tableName);
			while(true){
				hTableExecutorService = executorServiceQueue.pollFirst();
				setProgress(progress, "polled queue "+hTableExecutorService==null?"null":"success");
				
				if(hTableExecutorService==null){
					hTableExecutorService = new HTableExecutorService();
					setProgress(progress, "new HTableExecutorService()");
					String counterName = "connection create HTable";
					DRCounters.incClientTable(HBaseClientType.INSTANCE, counterName, clientName, tableName);
					logWithPoolInfo("created new HTableExecutorService", tableName);
					break;
				}
				if( ! hTableExecutorService.isExpired()){
	//				logger.warn("connection got pooled HTable executor");
					DRCounters.incClientTable(HBaseClientType.INSTANCE, "got pooled HTable executor", clientName, 
							tableName);
					break;//done.  we got an unexpired one, exit the while loop
				}

				//If we get here we're draining the queue of expired ExecutorServices.  We could do this
				// in a background thread, but this should eventually accomplish the same thing
				// with fewer moving parts.  Goal is to be able to allow large queue sizes for bursts
				// but to free up the memory of hundreds or thousands of threads in quiet times when
				// other things might be bursting
				hTableExecutorService.exec.shutdown();
				logWithPoolInfo("discarded expired HTableExecutorService", tableName);
				hTableExecutorService = null;//release it and loop around again
			}

			HConnection hConnection = HConnectionManager.getConnection(hBaseConfiguration);
			setProgress(progress, "got hConnection "+hConnection==null?"null":"");
			hTable = new HTable(StringByteTool.getUtf8Bytes(tableName), hConnection,
					hTableExecutorService.exec);
			setProgress(progress, "created HTable");
			activeHTables.put(hTable, hTableExecutorService);
			setProgress(progress, "added to activeHTables");
			hTable.getWriteBuffer().clear();
			setProgress(progress, "cleared HTable write buffer");
			hTable.setAutoFlush(false);
			setProgress(progress, "set HTable autoFlush false");
			recordSlowCheckout(System.currentTimeMillis() - checkoutRequestStartMs, tableName);
			logIfInconsistentCounts(true, tableName);
			Assert.assertNotNull(hTable);
			return hTable;
		}catch(Exception e){
			if(hTable!=null){
				activeHTables.remove(hTable);
				setProgress(progress, "removed from activeHTables");
			}
			hTableSemaphore.release();//HTable didn't make it out into the wild, so we know it can't be checked in later
			setProgress(progress, "released sempahore");
			throw new RuntimeException(e);
		}
	}
	
	protected void setProgress(MutableString progress, String s) {
		if(progress==null) { return; }
		progress.set(s);
	}


	@Override
	public void checkIn(HTable hTable, boolean possiblyTarnished){
		//do this first otherwise things may get hung up in the "active" map
		String tableName = StringByteTool.fromUtf8Bytes(hTable.getTableName());
		HTableExecutorService hTableExecutorService;
		try {
			hTableExecutorService = activeHTables.remove(hTable);
			hTable.getWriteBuffer().clear();
			if(hTableExecutorService==null){
				logWithPoolInfo("HTable returned to pool but HTableExecutorService not found", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable returned to pool but HTableExecutorService not found", 
						clientName, tableName);
				//don't release the semaphore
				return;
			}
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		try{
			hTableExecutorService.markLastCheckinMs();
			hTableExecutorService.purge();
			if(possiblyTarnished){//discard
				logWithPoolInfo("ThreadPoolExecutor possibly tarnished, discarding", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor possiblyTarnished", clientName, tableName);
				hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(hTableExecutorService.isDyingOrDead(tableName)){//discard
				logWithPoolInfo("ThreadPoolExecutor not reusable, discarding", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor isDyingOrDead", clientName, tableName);
				hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(!hTableExecutorService.isTaskQueueEmpty()){//discard
				logWithPoolInfo("ThreadPoolExecutor taskQueue not empty, discarding", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor taskQueue not empty", clientName, tableName);
				hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(!hTableExecutorService.waitForActiveThreadsToSettle(tableName)){//discard
				logWithPoolInfo("active thread count would not settle to 0", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor pool active threads won't quit", clientName, tableName);
				hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else{
				if(executorServiceQueue.offer(hTableExecutorService)){//keep it!
					DRCounters.incClientTable(HBaseClientType.INSTANCE, "connection HTable returned to pool", clientName, tableName);
				}else{//discard
					logWithPoolInfo("checkIn HTable but queue already full, so close and discard", tableName);
					DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor pool overflow", clientName, tableName);
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
		long startAquireMs = System.currentTimeMillis();
		SemaphoreTool.acquire(hTableSemaphore);
		long acquireTimeMs = System.currentTimeMillis() - startAquireMs;
		if(acquireTimeMs > LOG_SEMAPHORE_ACQUISITIONS_OVER_MS){
			logger.warn("acquiring semaphore took "+DrNumberFormatter.addCommas(acquireTimeMs)+"ms");
		}
	}

	protected synchronized void releaseSempahoreAndCheckConsistency(String tableName){
		hTableSemaphore.release();
		logIfInconsistentCounts(false, tableName);
	}

	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}

	@Override
	public Integer getTotalPoolSize(){
		return executorServiceQueue.size();
	}

	/*********************** logging ************************************/

	protected void recordSlowCheckout(long checkOutDurationMs, String tableName){
		if(!LOG_ACTIONS) { return; }
		if(checkOutDurationMs > 1){
			DRCounters.incClientTable(HBaseClientType.INSTANCE, "connection open > 1ms", clientName, tableName);
//			logger.warn("slow reserveConnection:"+checkOutDurationMs+"ms on "+clientName);
		}
	}

	static final int LEWAY = 0;

	public boolean areCountsConsistent(boolean checkOut) {
		int numActivePermits = hTableSemaphoreActivePermits();
		if(numActivePermits > maxSize) { return false; }
		int numActiveHTables = activeHTables.size();
		if(numActiveHTables > maxSize) { return false; }
		if(numActiveHTables > numActivePermits) { return false; }
		return true;
	}

	static final long THROTTLE_INCONSISTENT_LOG_EVERY_X_MS = 500;

	public void forceLogIfInconsistentCounts(boolean checkOut, String tableName){
		//ignore the LOG_ACTIONS variable
		innerLogIfInconsistentCounts(checkOut, tableName);
	}

	public void logIfInconsistentCounts(boolean checkOut, String tableName){
		if(!LOG_ACTIONS) { return; }
		innerLogIfInconsistentCounts(checkOut, tableName);
	}

	public void innerLogIfInconsistentCounts(boolean checkOut, String tableName){
		if(!areCountsConsistent(checkOut)){
			long msSinceLastLog = System.currentTimeMillis() - lastLoggedWarning;
			if(msSinceLastLog < THROTTLE_INCONSISTENT_LOG_EVERY_X_MS){ return; }
			logWithPoolInfo("inconsistent pool counts on "+(checkOut?"checkOut":"checkIn"), tableName);
		}
		lastLoggedWarning = System.currentTimeMillis();
	}

	public void forceLogWithPoolInfo(String message, String tableName) {
		//ignore the LOG_ACTIONS variable
		innerLogWithPoolInfo(message, tableName);
	}

	protected void logWithPoolInfo(String message, String tableName){
		if(!LOG_ACTIONS) { return; }
		innerLogWithPoolInfo(message, tableName);
	}

	protected void innerLogWithPoolInfo(String message, String tableName) {
		logger.warn(getPoolInfoLogMessage(tableName)+", "+message);
	}

	protected String getPoolInfoLogMessage(String tableName){
		return "max="+maxSize
				+", blocked="+hTableSemaphore.getQueueLength()
				+", idle="+executorServiceQueue.size()
				+", permits="+hTableSemaphoreActivePermits()
				+", HTables="+activeHTables.size()
				+", client="+clientName
				+", table="+tableName;
	}

	protected int hTableSemaphoreActivePermits(){
		return maxSize - hTableSemaphore.availablePermits();//seems to always be 1 lower?
	}

}
