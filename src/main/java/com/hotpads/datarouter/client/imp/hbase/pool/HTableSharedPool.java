package com.hotpads.datarouter.client.imp.hbase.pool;

import java.util.Map;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.MapTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.SemaphoreTool;
import com.hotpads.util.datastructs.MutableString;


public class HTableSharedPool  implements HTablePool{
	protected Logger logger = Logger.getLogger(getClass());

	protected static Boolean LOG_ACTIONS = true;

	protected Long lastLoggedWarning = 0L;

	protected Configuration hBaseConfiguration;
	protected String clientName;
	protected Integer maxSize;

	//this turned out weird:
	// using Semaphore and BlockingDequeue here.  evolutionary complexity, but possibly more flexible
	protected Semaphore hTableSemaphore;
	//protected BlockingDeque<HTableExecutorService> executorServiceQueue;
	protected Map<HTable,HTableExecutorService> activeHTables;

	protected Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName;

	protected HTableExecutorService executorService = new HTableExecutorService();


	public HTableSharedPool(Configuration hBaseConfiguration,
			String clientName, int maxSize,
			Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName){
		this.hBaseConfiguration = hBaseConfiguration;
		this.clientName = clientName;
		this.maxSize = maxSize;
		this.hTableSemaphore = new Semaphore(maxSize);
		this.activeHTables = MapTool.createConcurrentHashMap();
		this.primaryKeyClassByName = primaryKeyClassByName;
	}

	@Override
	public HTable checkOut(String tableName, MutableString progress){
		long checkoutRequestStartMs = System.currentTimeMillis();
		checkConsistencyAndAcquireSempahore(tableName);
		setProgress(progress, "passed semaphore");
		HTable hTable = null;
		try{
			DRCounters.incSuffixClientTable(HBaseClientType.INSTANCE, "connection getHTable", clientName, tableName);

			HConnection hConnection = HConnectionManager.getConnection(hBaseConfiguration);
			setProgress(progress, "got hConnection "+hConnection==null?"null":"");
			hTable = new HTable(StringByteTool.getUtf8Bytes(tableName), hConnection,
					executorService.exec);
			setProgress(progress, "created HTable");
			activeHTables.put(hTable, executorService);
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
				DRCounters.incSuffixClientTable(HBaseClientType.INSTANCE, "HTable returned to pool but HTableExecutorService not found", 
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
				DRCounters.incSuffixClientTable(HBaseClientType.INSTANCE, "HTable executor possiblyTarnished", clientName, tableName);
				//hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(hTableExecutorService.isDyingOrDead(tableName)){//discard
				logWithPoolInfo("ThreadPoolExecutor not reusable, discarding", tableName);
				DRCounters.incSuffixClientTable(HBaseClientType.INSTANCE, "HTable executor isDyingOrDead", clientName, tableName);
				//hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(!hTableExecutorService.isTaskQueueEmpty()){//discard
				logWithPoolInfo("ThreadPoolExecutor taskQueue not empty, discarding", tableName);
				DRCounters.incSuffixClientTable(HBaseClientType.INSTANCE, "HTable executor taskQueue not empty", clientName, tableName);
				//hTableExecutorService.terminateAndBlockUntilFinished(tableName);
			}else if(!hTableExecutorService.waitForActiveThreadsToSettle(tableName)){//discard
				logWithPoolInfo("active thread count would not settle to 0", tableName);
				DRCounters.incSuffixClientTable(HBaseClientType.INSTANCE, "HTable executor pool active threads won't quit", clientName, tableName);
				//hTableExecutorService.terminateAndBlockUntilFinished(tableName);
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


	/*********************** logging ************************************/

	protected void recordSlowCheckout(long checkOutDurationMs, String tableName){
		if(!LOG_ACTIONS) { return; }
		if(checkOutDurationMs > 1){
			DRCounters.incSuffixClientTable(HBaseClientType.INSTANCE, "connection open > 1ms", clientName, tableName);
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
				+", permits="+hTableSemaphoreActivePermits()
				+", HTables="+activeHTables.size()
				+", client="+clientName
				+", table="+tableName;
	}

	protected int hTableSemaphoreActivePermits(){
		return maxSize - hTableSemaphore.availablePermits();//seems to always be 1 lower?
	}

	@Override
	public Integer getTotalPoolSize(){
		return executorService.exec.getActiveCount();
	}
}
