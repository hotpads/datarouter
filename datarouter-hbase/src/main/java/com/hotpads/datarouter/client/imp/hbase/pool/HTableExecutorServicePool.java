package com.hotpads.datarouter.client.imp.hbase.pool;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTable;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.SemaphoreTool;
import com.hotpads.util.core.concurrent.ThreadTool;
import com.hotpads.util.datastructs.MutableString;

/*
 * Despite the name HTable, this pool stores "connections" to all tables.
 */
public class HTableExecutorServicePool
implements HTablePool{
	private static final Logger logger = LoggerFactory.getLogger(HTableExecutorServicePool.class);

	private static final boolean LOG_ACTIONS = true;
	private static final long LOG_SEMAPHORE_ACQUISITIONS_OVER_MS = 2000L;
	private static final long THROTTLE_INCONSISTENT_LOG_EVERY_X_MS = 500;

	//provided via constructor
	private final HConnection hConnection;//one connection per cluster
	private final String clientName;
	private final int maxSize;
	private final Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName;
	
	//used for pooling connections
	private final Semaphore hTableSemaphore;
	private final BlockingDeque<HTableExecutorService> executorServiceQueue;
	private final Map<HTable,HTableExecutorService> activeHTables;

	private volatile boolean shuttingDown;
	private volatile long lastLoggedWarning = 0L;
	
	public HTableExecutorServicePool(HBaseAdmin hBaseAdmin, String clientName, int maxSize,
			Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName){
		this.hConnection = hBaseAdmin.getConnection();
		this.clientName = clientName;
		this.maxSize = maxSize;
		this.primaryKeyClassByName = primaryKeyClassByName;
		
		this.hTableSemaphore = new Semaphore(maxSize);
		this.executorServiceQueue = new LinkedBlockingDeque<>(maxSize);
		this.activeHTables = new ConcurrentHashMap<>();
	}

	
	@Override
	public HTable checkOut(String tableName, MutableString progress){
		if(shuttingDown){
			return null;
		}
		long checkoutRequestStartMs = System.currentTimeMillis();
		checkConsistencyAndAcquireSempahore(tableName);
		setProgress(progress, "passed semaphore");
		HTableExecutorService hTableExecutorService = null;
		HTable hTable = null;
		try{
			DRCounters.incClientTable(HBaseClientType.INSTANCE, "connection getHTable", clientName, tableName);
			while(true){
				hTableExecutorService = executorServiceQueue.pollFirst();
				setProgress(progress, "polled queue " + hTableExecutorService == null ? "null" : "success");
				
				if(hTableExecutorService==null){
					hTableExecutorService = new HTableExecutorService();
					setProgress(progress, "new HTableExecutorService()");
					String counterName = "connection create HTable";
					DRCounters.incClientTable(HBaseClientType.INSTANCE, counterName, clientName, tableName);
					logWithPoolInfo("created new HTableExecutorService", tableName);
					break;
				}
				if( ! hTableExecutorService.isExpired()){
					// logger.warn("connection got pooled HTable executor");
					DRCounters.incClientTable(HBaseClientType.INSTANCE, "got pooled HTable executor", clientName,
							tableName);
					break;// done. we got an unexpired one, exit the while loop
				}

				//If we get here we're draining the queue of expired ExecutorServices.  We could do this
				// in a background thread, but this should eventually accomplish the same thing
				// with fewer moving parts.  Goal is to be able to allow large queue sizes for bursts
				// but to free up the memory of hundreds or thousands of threads in quiet times when
				// other things might be bursting
				hTableExecutorService.getExec().shutdown();
				logWithPoolInfo("discarded expired HTableExecutorService", tableName);
				hTableExecutorService = null;//release it and loop around again
			}
			hTable = new HTable(StringByteTool.getUtf8Bytes(tableName), hConnection, hTableExecutorService.getExec());
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
			if(hTable != null){
				activeHTables.remove(hTable);
				setProgress(progress, "removed from activeHTables");
			}
			hTableSemaphore.release();//HTable didn't make it out into the wild, so we know it can't be checked in later
			setProgress(progress, "released sempahore");
			throw new RuntimeException(e);
		}
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

	@Override
	public Integer getTotalPoolSize(){
		return executorServiceQueue.size();
	}
	
	@Override
	public void shutdown(){
		shuttingDown = true;
		if(hTableSemaphoreActivePermits() != 0){
			logger.info("Still " + hTableSemaphoreActivePermits() + "active hTables");
			ThreadTool.sleep(5000);
		}
		for(HTableExecutorService executorService : executorServiceQueue){
			executorService.terminateAndBlockUntilFinished("shutdown");
		}
		try{
			hConnection.close();
		}catch (IOException e){
			logger.error("Error while closing hConnection", e);
			throw new RuntimeException(e);
		}
	}
	
	
	/********************** private ****************************/

	//for some reason, synchronizing this method wreaks havoc and stops all progress
	private /*synchronized*/ void checkConsistencyAndAcquireSempahore(String tableName){
		logIfInconsistentCounts(true, tableName);
		long startAquireMs = System.currentTimeMillis();
		SemaphoreTool.acquire(hTableSemaphore);
		long acquireTimeMs = System.currentTimeMillis() - startAquireMs;
		if(acquireTimeMs > LOG_SEMAPHORE_ACQUISITIONS_OVER_MS){
			logger.warn("acquiring semaphore took "+DrNumberFormatter.addCommas(acquireTimeMs)+"ms");
		}
	}

	private synchronized void releaseSempahoreAndCheckConsistency(String tableName){
		hTableSemaphore.release();
		logIfInconsistentCounts(false, tableName);
	}

	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}

	private int hTableSemaphoreActivePermits(){
		return maxSize - hTableSemaphore.availablePermits();//seems to always be 1 lower?
	}
	
	private void setProgress(MutableString progress, String s){
		if(progress == null){
			return;
		}
		progress.set(s);
	}

	
	/*********************** logging ************************************/

	private void recordSlowCheckout(long checkOutDurationMs, String tableName){
		if(!LOG_ACTIONS) {
			return;
		}
		if(checkOutDurationMs > 1){
			DRCounters.incClientTable(HBaseClientType.INSTANCE, "connection open > 1ms", clientName, tableName);
//			logger.warn("slow reserveConnection:"+checkOutDurationMs+"ms on "+clientName);
		}
	}


	private boolean areCountsConsistent(){
		int numActivePermits = hTableSemaphoreActivePermits();
		if(numActivePermits > maxSize){
			return false;
		}
		int numActiveHTables = activeHTables.size();
		if(numActiveHTables > maxSize){
			return false;
		}
		if(numActiveHTables > numActivePermits){
			return false;
		}
		return true;
	}


	public void forceLogIfInconsistentCounts(boolean checkOut, String tableName){
		//ignore the LOG_ACTIONS variable
		innerLogIfInconsistentCounts(checkOut, tableName);
	}

	private void logIfInconsistentCounts(boolean checkOut, String tableName){
		if(!LOG_ACTIONS){
			return;
		}
		innerLogIfInconsistentCounts(checkOut, tableName);
	}

	private void innerLogIfInconsistentCounts(boolean checkOut, String tableName){
		if(!areCountsConsistent()){
			long msSinceLastLog = System.currentTimeMillis() - lastLoggedWarning;
			if(msSinceLastLog < THROTTLE_INCONSISTENT_LOG_EVERY_X_MS){
				return;
			}
			logWithPoolInfo("inconsistent pool counts on "+(checkOut?"checkOut":"checkIn"), tableName);
		}
		lastLoggedWarning = System.currentTimeMillis();
	}

	private void forceLogWithPoolInfo(String message, String tableName) {
		//ignore the LOG_ACTIONS variable
		innerLogWithPoolInfo(message, tableName);
	}

	private void logWithPoolInfo(String message, String tableName){
		if(!LOG_ACTIONS){ 
			return;
		}
		innerLogWithPoolInfo(message, tableName);
	}

	private void innerLogWithPoolInfo(String message, String tableName) {
		logger.warn(getPoolInfoLogMessage(tableName)+", "+message);
	}

	private String getPoolInfoLogMessage(String tableName){
		return "max="+maxSize
				+", blocked="+hTableSemaphore.getQueueLength()
				+", idle="+executorServiceQueue.size()
				+", permits="+hTableSemaphoreActivePermits()
				+", HTables="+activeHTables.size()
				+", client="+clientName
				+", table="+tableName;
	}

}
