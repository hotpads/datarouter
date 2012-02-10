package com.hotpads.datarouter.client.imp.hbase;

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
	
	
	protected Map<HTable,HTableExecutorService> hTableExecutorServiceByHTable;
	
	protected Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName;
	
	
	public HTableExecutorServicePool(Configuration hBaseConfiguration, 
			String clientName, int maxSize,
			Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName){
		this.hBaseConfiguration = hBaseConfiguration;
		this.clientName = clientName;
		this.maxSize = maxSize;
		this.hTableSemaphore = new Semaphore(maxSize);
		this.executorServiceQueue = new LinkedBlockingDeque<HTableExecutorService>(maxSize);
		this.hTableExecutorServiceByHTable = MapTool.createConcurrentHashMap();
		this.primaryKeyClassByName = primaryKeyClassByName;
	}
	
	
	@Override
	public HTable checkOut(String name){
		SemaphoreTool.acquire(hTableSemaphore);
		DRCounters.inc("connection getHTable "+name);
		HTableExecutorService hTableExecutorService = null;
		while(true){
			hTableExecutorService = executorServiceQueue.pollFirst();
			if(hTableExecutorService==null){
				hTableExecutorService = new HTableExecutorService();
				String counterName = "connection create HTable "+name;
				DRCounters.inc(counterName);
				logger.warn("created new HTable ThreadPool Executor for table "+name+getPoolInfoLogMessage());
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
		try{
			HConnection hConnection = HConnectionManager.getConnection(hBaseConfiguration);
			hTable = new HTable(StringByteTool.getUtf8Bytes(name), hConnection, 
					hTableExecutorService.exec);
			hTableExecutorServiceByHTable.put(hTable, hTableExecutorService);
			hTable.getWriteBuffer().clear();
			hTable.setAutoFlush(false);
			return hTable;
		}catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
	}
	
	
	@Override
	public void checkIn(HTable hTable, boolean possiblyTarnished){
		hTableSemaphore.release();
		hTable.getWriteBuffer().clear();
		String name = StringByteTool.fromUtf8Bytes(hTable.getTableName());
		HTableExecutorService hTableExecutorService = hTableExecutorServiceByHTable.remove(hTable);
		if(hTableExecutorService==null){
			logger.warn("HTable returned to pool but HTableExecutorService not found "+getPoolInfoLogMessage());
			DRCounters.inc("HTable returned to pool but HTableExecutorService not found");
			return;
		}
		hTableExecutorService.markLastCheckinMs();
		ThreadPoolExecutor exec = hTableExecutorService.exec;
		exec.purge();
		if(possiblyTarnished){//discard
			logger.warn("ThreadPoolExecutor possibly tarnished, discarding.  table:"+name+getPoolInfoLogMessage());
			DRCounters.inc("HTable executor possiblyTarnished "+name);	
			hTableExecutorService.terminateAndBlockUntilFinished(name);
		}else if(hTableExecutorService.isDyingOrDead(name)){//discard
			logger.warn("ThreadPoolExecutor not reusable, discarding.  table:"+name+getPoolInfoLogMessage());
			DRCounters.inc("HTable executor isDyingOrDead "+name);	
			hTableExecutorService.terminateAndBlockUntilFinished(name);
		}else if(!hTableExecutorService.isTaskQueueEmpty()){//discard
			logger.warn("ThreadPoolExecutor taskQueue not empty, discarding.  table:"+name+getPoolInfoLogMessage());
			DRCounters.inc("HTable executor taskQueue not empty "+name);	
			hTableExecutorService.terminateAndBlockUntilFinished(name);
		}else if(!hTableExecutorService.waitForActiveThreadsToSettle(name)){//discard
			logger.warn("active thread count would not settle to 0, table="+name+getPoolInfoLogMessage());
			DRCounters.inc("HTable executor pool active threads won't quit "+name);	
			hTableExecutorService.terminateAndBlockUntilFinished(name);
		}else{
			if(executorServiceQueue.offer(hTableExecutorService)){//keep it!
				DRCounters.inc("connection HTable returned to pool "+name);
			}else{//discard
				logger.warn("checkIn HTable but queue already full, so close and discard, table="+name+getPoolInfoLogMessage());
				DRCounters.inc("HTable executor pool overflow");	
				hTableExecutorService.terminateAndBlockUntilFinished(name);
			}
		}
	}
	
	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}
	
	public Integer getTotalPoolSize(){
		return executorServiceQueue.size();
	}
	
	protected String getPoolInfoLogMessage(){
		return ", clientName="+clientName
				+", Executors(active)="+hTableExecutorServiceByHTable.size()
				+", Executors(idle)="+executorServiceQueue.size()
				+", HTables(available)="+hTableSemaphore.availablePermits()
				+", HTables(waiting)="+hTableSemaphore.getQueueLength();
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
		
		public boolean waitForActiveThreadsToSettle(String tableNameForLog){
			if(exec.getActiveCount()==0){ return true; }
			ThreadTool.sleep(1);
			if(exec.getActiveCount()==0){
				logger.warn("had to sleep a little to let threads finish, table:"+tableNameForLog);
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
	
}
