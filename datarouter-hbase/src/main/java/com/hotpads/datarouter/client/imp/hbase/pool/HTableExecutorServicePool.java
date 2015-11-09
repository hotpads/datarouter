package com.hotpads.datarouter.client.imp.hbase.pool;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.client.imp.hbase.factory.HBaseOptions;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.util.core.concurrent.ExecutorServiceTool;
import com.hotpads.util.core.concurrent.SemaphoreTool;
import com.hotpads.util.core.concurrent.ThreadTool;
import com.hotpads.util.datastructs.MutableString;

/*
 * Despite the name HTable, this pool stores "connections" to all tables.
 */
public class HTableExecutorServicePool
implements HTablePool{
	private static final Logger logger = LoggerFactory.getLogger(HTableExecutorServicePool.class);

	private static final int DEFAULT_MAX_HTABLES = 10;
	private static final int DEFAULT_MIN_THREADS_PER_HTABLE = 1;
	//practically, you will get only one thread per regionserver, but it doesn't hurt to have a high ceiling that won't
	// exhaust all server threads
	private static final int DEFAULT_MAX_THREADS_PER_HTABLE = 1024;
	private static final boolean LOG_ACTIONS = true;
	private static final long LOG_SEMAPHORE_ACQUISITIONS_OVER_MS = 2000L;
	private static final long THROTTLE_INCONSISTENT_LOG_EVERY_X_MS = 500;

	//provided via constructor
	private final Connection connection;
	private final String clientName;
	private final Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName;
	private final int maxHTables;
	private final int minThreadsPerHTable;
	private final int maxThreadsPerHTable;

	//used for pooling connections
	private final Semaphore htableSemaphore;
	private final BlockingQueue<HTableExecutorService> executorServiceQueue;
	private final Map<Table,HTableExecutorService> activeHTables;

	private volatile boolean shuttingDown;
	private volatile long lastLoggedWarning = 0L;

	public HTableExecutorServicePool(HBaseOptions hbaseOptions, Connection connection, HBaseAdmin hbaseAdmin,
			String clientName, Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName){
		this.connection = connection;
		this.clientName = clientName;
		this.primaryKeyClassByName = primaryKeyClassByName;
		this.maxHTables = hbaseOptions.maxHTables(DEFAULT_MAX_HTABLES);
		this.minThreadsPerHTable = hbaseOptions.minThreadsPerHTable(DEFAULT_MIN_THREADS_PER_HTABLE);
		this.maxThreadsPerHTable = hbaseOptions.maxThreadsPerHTable(DEFAULT_MAX_THREADS_PER_HTABLE);

		this.htableSemaphore = new Semaphore(maxHTables);
		this.executorServiceQueue = new LinkedBlockingQueue<>(maxHTables);
		this.activeHTables = new ConcurrentHashMap<>();
	}


	@Override
	public Table checkOut(String tableName, MutableString progress){
		if(shuttingDown){
			return null;
		}
		long checkoutRequestStartMs = System.currentTimeMillis();
		checkConsistencyAndAcquireSempahore(tableName);
		setProgress(progress, "passed semaphore");
		HTableExecutorService htableExecutorService = null;
		Table htable = null;
		try{
			DRCounters.incClientTable(HBaseClientType.INSTANCE, "connection getHTable", clientName, tableName);
			while(true){
				htableExecutorService = executorServiceQueue.poll();
				setProgress(progress, "polled queue " + (htableExecutorService == null ? "null" : "success"));

				if(htableExecutorService==null){
					htableExecutorService = new HTableExecutorService(minThreadsPerHTable, maxThreadsPerHTable);
					setProgress(progress, "new HTableExecutorService()");
					String counterName = "connection create HTable";
					DRCounters.incClientTable(HBaseClientType.INSTANCE, counterName, clientName, tableName);
					logWithPoolInfo("created new HTableExecutorService", tableName);
					break;
				}
				if( ! htableExecutorService.isExpired()){
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
				ExecutorServiceTool.shutdown(htableExecutorService.getExec());
				logWithPoolInfo("discarded expired HTableExecutorService", tableName);
				htableExecutorService = null;//release it and loop around again
			}
			htable = connection.getTable(TableName.valueOf(tableName), htableExecutorService.getExec());
			setProgress(progress, "created HTable");
			activeHTables.put(htable, htableExecutorService);
			setProgress(progress, "added to activeHTables");
			recordSlowCheckout(System.currentTimeMillis() - checkoutRequestStartMs, tableName);
			logIfInconsistentCounts(true, tableName);
			return htable;
		}catch(Exception e){
			if(htable != null){
				activeHTables.remove(htable);
				setProgress(progress, "removed from activeHTables");
			}
			htableSemaphore.release();//HTable didn't make it out into the wild, so we know it can't be checked in later
			setProgress(progress, "released sempahore");
			throw new RuntimeException(e);
		}
	}


	@Override
	public void checkIn(Table htable, boolean possiblyTarnished){
		//do this first otherwise things may get hung up in the "active" map
		String tableName = htable.getName().getNameAsString();
		HTableExecutorService htableExecutorService;
		try {
			htableExecutorService = activeHTables.remove(htable);
			if(htableExecutorService==null){
				logWithPoolInfo("HTable returned to pool but HTableExecutorService not found", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable returned to pool but HTableExecutorService"
						+ " not found", clientName, tableName);
				//don't release the semaphore
				return;
			}
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		try{
			htableExecutorService.markLastCheckinMs();
			htableExecutorService.purge();
			if(possiblyTarnished){//discard
				logWithPoolInfo("ThreadPoolExecutor possibly tarnished, discarding", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor possiblyTarnished", clientName,
						tableName);
				htableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(htableExecutorService.isDyingOrDead(tableName)){//discard
				logWithPoolInfo("ThreadPoolExecutor not reusable, discarding", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor isDyingOrDead", clientName,
						tableName);
				htableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(!htableExecutorService.isTaskQueueEmpty()){//discard
				logWithPoolInfo("ThreadPoolExecutor taskQueue not empty, discarding", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor taskQueue not empty", clientName,
						tableName);
				htableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(!htableExecutorService.waitForActiveThreadsToSettle(tableName)){//discard
				logWithPoolInfo("active thread count would not settle to 0", tableName);
				DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor pool active threads won't quit",
						clientName, tableName);
				htableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else{
				if(executorServiceQueue.offer(htableExecutorService)){//keep it!
					DRCounters.incClientTable(HBaseClientType.INSTANCE, "connection HTable returned to pool",
							clientName, tableName);
				}else{//discard
					logWithPoolInfo("checkIn HTable but queue already full, so close and discard", tableName);
					DRCounters.incClientTable(HBaseClientType.INSTANCE, "HTable executor pool overflow", clientName,
							tableName);
					htableExecutorService.terminateAndBlockUntilFinished(tableName);
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
		if(htableSemaphoreActivePermits() != 0){
			final int sleepMs = 5000;
			logger.warn("Still " + htableSemaphoreActivePermits() + "active hTables.  Sleeping " + sleepMs + "ms");
			ThreadTool.sleep(sleepMs);
		}
		for(HTableExecutorService executorService : executorServiceQueue){
			executorService.terminateAndBlockUntilFinished("shutdown");
		}

		//Close HConnection and use stopProxy = true to join the HBaseClient.Connection thread.
		//TODO what is the hbase-1 replacement for this?
//		HConnectionManager.deleteConnection(hconnection.getConfiguration(), true);
	}


	/********************** private ****************************/

	//for some reason, synchronizing this method wreaks havoc and stops all progress
	private /*synchronized*/ void checkConsistencyAndAcquireSempahore(String tableName){
		logIfInconsistentCounts(true, tableName);
		long startAquireMs = System.currentTimeMillis();
		SemaphoreTool.acquire(htableSemaphore);
		long acquireTimeMs = System.currentTimeMillis() - startAquireMs;
		if(acquireTimeMs > LOG_SEMAPHORE_ACQUISITIONS_OVER_MS){
			logger.warn("acquiring semaphore took "+DrNumberFormatter.addCommas(acquireTimeMs)+"ms");
		}
	}

	private synchronized void releaseSempahoreAndCheckConsistency(String tableName){
		htableSemaphore.release();
		logIfInconsistentCounts(false, tableName);
	}

	public Class<? extends PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}

	private int htableSemaphoreActivePermits(){
		return maxHTables - htableSemaphore.availablePermits();//seems to always be 1 lower?
	}

	private void setProgress(MutableString progress, String message){
		if(progress == null){
			return;
		}
		progress.set(message);
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
		int numActivePermits = htableSemaphoreActivePermits();
		if(numActivePermits > maxHTables){
			return false;
		}
		int numActiveHTables = activeHTables.size();
		if(numActiveHTables > maxHTables){
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
		return "max="+maxHTables
				+", blocked="+htableSemaphore.getQueueLength()
				+", idle="+executorServiceQueue.size()
				+", permits="+htableSemaphoreActivePermits()
				+", HTables="+activeHTables.size()
				+", client="+clientName
				+", table="+tableName;
	}

}
