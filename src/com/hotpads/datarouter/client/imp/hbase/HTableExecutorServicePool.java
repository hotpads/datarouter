package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
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

public class HTableExecutorServicePool implements HTablePool{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected Long lastLoggedWarning = 0L;
	
	protected Configuration hBaseConfiguration;
	protected Integer maxSize;
	
	protected BlockingDeque<HTableExecutorService> queue;
	protected Map<HTable,HTableExecutorService> hTableExecutorServiceByHTable;
	
	protected Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName;
	
	
	public HTableExecutorServicePool(Configuration hBaseConfiguration, 
			Collection<String> names, int maxSize,
			Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName){
		this.hBaseConfiguration = hBaseConfiguration;
		this.maxSize = maxSize;
		this.queue = new LinkedBlockingDeque<HTableExecutorService>(maxSize);
		this.hTableExecutorServiceByHTable = MapTool.createConcurrentHashMap();
		this.primaryKeyClassByName = primaryKeyClassByName;
	}
	
	
	@Override
	public HTable checkOut(String name){
		DRCounters.inc("connection getHTable "+name);
		HTableExecutorService hTableExecutorService = null;
		while(true){
			hTableExecutorService = queue.pollFirst();
			if(hTableExecutorService==null){
				hTableExecutorService = new HTableExecutorService();
				String counterName = "connection create HTable "+name;
				DRCounters.inc(counterName);
				break;
			}
			if(hTableExecutorService.isExpired()){
				//TODO background thread that actively discards expired pools
				logger.warn("discarded expired executorService");
				hTableExecutorService = null;//release it
			}else{
				DRCounters.inc("connection got pooled HTable executor");
			}
		}
		
		HTable hTable = null;
		try{
			HConnection hConnection = HConnectionManager.getConnection(hBaseConfiguration);
			hTable = new HTable(StringByteTool.getUtf8Bytes(name), hConnection, 
					hTableExecutorService.exec);
			hTableExecutorServiceByHTable.put(hTable, hTableExecutorService);
			hTable.getWriteBuffer().clear();
			hTable.setAutoFlush(false);
		}catch(IOException ioe){
			throw new RuntimeException(ioe);
		}
		return hTable;
	}
	
	
	@Override
	public void checkIn(HTable hTable, boolean possiblyTarnished){
		hTable.getWriteBuffer().clear();
		String name = StringByteTool.fromUtf8Bytes(hTable.getTableName());
		HTableExecutorService hTableExecutorService = hTableExecutorServiceByHTable.get(hTable);
		hTableExecutorServiceByHTable.remove(hTable);
		hTableExecutorService.markLastCheckinMs();
		ThreadPoolExecutor exec = hTableExecutorService.exec;
		exec.purge();
		if(possiblyTarnished){
			logger.warn("ThreadPoolExecutor possibly tarnished, discarding.  table:"+name);
			DRCounters.inc("HTable executor possibly tarnished "+name);	
			hTableExecutorService.terminate();
		}else if(!hTableExecutorService.isReusable()){//discard
			logger.warn("ThreadPoolExecutor not reusable, discarding.  table:"+name);
			DRCounters.inc("HTable executor not reusable "+name);	
			hTableExecutorService.terminate();
		}else if(queue.offer(hTableExecutorService)){//reuse
			DRCounters.inc("connection HTable returned to pool "+name);
		}else{//discard
			logger.warn("checkIn HTable but queue already full, so close and discard, table="+name);
			DRCounters.inc("HTable executor pool overflow");	
			hTableExecutorService.terminate();
		}
	}
	
	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}
	
	public Integer getTotalPoolSize(){
		return queue.size();
	}
	
	
	
	public static class HTableExecutorService{
		protected Logger logger = Logger.getLogger(getClass());
		
		public static final Long TIMEOUT_MS = 15 * 1000L;//15 seconds
		
		protected ThreadPoolExecutor exec;
//		protected ExecutorService executorService;
		protected Long createdMs;
		protected Long lastCheckinMs;
		
		public HTableExecutorService() {
			this.exec = new ThreadPoolExecutor(1, Integer.MAX_VALUE,
			        60, TimeUnit.SECONDS,
			        new SynchronousQueue<Runnable>());
//			this.executorService = Executors.newCachedThreadPool();
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
		
		public boolean isReusable(){	
			if(exec.isShutdown()){
				logger.warn("executor isShutdown");
				return false;
			}
			if(exec.isTerminated()){
				logger.warn("executor isTerminated");
				return false;
			}
			if(exec.isTerminating()){
				logger.warn("executor isTerminating");
				return false;
			}
			if(exec.getQueue().size() > 0){
				logger.warn("executor has "+exec.getQueue().size()+" queued tasks");
				return false;
			}
			if(exec.getActiveCount() > 0){
				logger.warn("executor has "+exec.getActiveCount()+" active threads");
				return false;
			}
			
			return true;//should be nice and clean for the next HTable
		}
		
		public void terminate(){
			exec.shutdownNow();//should not block
		}
	}
	
}
