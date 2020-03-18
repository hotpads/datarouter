/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.hbase.pool;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.client.HBaseOptions;
import io.datarouter.client.hbase.config.DatarouterHBaseSettingRoot;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.SemaphoreTool;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.mutable.MutableString;
import io.datarouter.util.number.NumberFormatter;

public class HBaseTablePool{
	private static final Logger logger = LoggerFactory.getLogger(HBaseTablePool.class);

	private static final int DEFAULT_MIN_THREADS_PER_HTABLE = 1;
	//practically, you will get only one thread per regionserver, but it doesn't hurt to have a high ceiling that won't
	// exhaust all server threads
	private static final int DEFAULT_MAX_THREADS_PER_HTABLE = 1024;
	private static final boolean LOG_ACTIONS = true;
	private static final long LOG_SEMAPHORE_ACQUISITIONS_OVER_MS = 2000L;
	private static final long THROTTLE_INCONSISTENT_LOG_EVERY_X_MS = 500;

	//provided via constructor
	private final Connection connection;
	private final ClientId clientId;
	private final ClientType<?,?> clientType;
	private final int maxHTables;
	private final int minThreadsPerHTable;
	private final int maxThreadsPerHTable;

	//used for pooling connections
	private final Semaphore htableSemaphore;
	private final BlockingQueue<HBaseTableExecutorService> executorServiceQueue;
	private final Map<Table,HBaseTableExecutorService> activeHTables;

	private volatile boolean shuttingDown;
	private volatile long lastLoggedWarning = 0L;

	public HBaseTablePool(
			HBaseOptions hbaseOptions,
			DatarouterHBaseSettingRoot datarouterHBaseSettingRoot,
			Connection connection,
			ClientId clientId,
			ClientType<?,?> clientType){
		this.connection = connection;
		this.clientId = clientId;
		this.clientType = clientType;
		this.maxHTables = hbaseOptions.maxHTables(clientId.getName(), datarouterHBaseSettingRoot.executorThreadCount);
		this.minThreadsPerHTable = hbaseOptions.minThreadsPerHTable(clientId.getName(), DEFAULT_MIN_THREADS_PER_HTABLE);
		this.maxThreadsPerHTable = hbaseOptions.maxThreadsPerHTable(clientId.getName(), DEFAULT_MAX_THREADS_PER_HTABLE);

		this.htableSemaphore = new Semaphore(maxHTables);
		this.executorServiceQueue = new LinkedBlockingQueue<>(maxHTables);
		this.activeHTables = new ConcurrentHashMap<>();
	}

	public Table checkOut(String tableName, MutableString progress){
		if(shuttingDown){
			return null;
		}
		long checkoutRequestStartMs = System.currentTimeMillis();
		checkConsistencyAndAcquireSempahore(tableName);
		setProgress(progress, "passed semaphore");
		HBaseTableExecutorService htableExecutorService = null;
		Table htable = null;
		try{
			DatarouterCounters.incClientTable(clientType, "connection getHTable", clientId.getName(), tableName, 1L);
			while(true){
				htableExecutorService = executorServiceQueue.poll();
				setProgress(progress, "polled queue " + (htableExecutorService == null ? "null" : "success"));

				if(htableExecutorService == null){
					htableExecutorService = new HBaseTableExecutorService(minThreadsPerHTable, maxThreadsPerHTable);
					setProgress(progress, "new HTableExecutorService()");
					String counterName = "connection create HTable";
					DatarouterCounters.incClientTable(clientType, counterName, clientId.getName(), tableName, 1L);
					logWithPoolInfo("created new HTableExecutorService", tableName);
					break;
				}
				if(!htableExecutorService.isExpired()){
					DatarouterCounters.incClientTable(clientType, "got pooled HTable executor", clientId.getName(),
							tableName, 1L);
					break;// done. we got an unexpired one, exit the while loop
				}

				//If we get here we're draining the queue of expired ExecutorServices.  We could do this
				// in a background thread, but this should eventually accomplish the same thing
				// with fewer moving parts.  Goal is to be able to allow large queue sizes for bursts
				// but to free up the memory of hundreds or thousands of threads in quiet times when
				// other things might be bursting
				ExecutorServiceTool.shutdown(htableExecutorService.getExec(), Duration.ofDays(1));
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

	public void checkIn(Table htable, boolean possiblyTarnished){
		//do this first otherwise things may get hung up in the "active" map
		String tableName = htable.getName().getNameAsString();
		HBaseTableExecutorService htableExecutorService;
		try{
			htableExecutorService = activeHTables.remove(htable);
			if(htableExecutorService == null){
				logWithPoolInfo("HTable returned to pool but HTableExecutorService not found", tableName);
				DatarouterCounters.incClientTable(clientType, "HTable returned to pool but HTableExecutorService"
						+ " not found", clientId.getName(), tableName, 1L);
				//don't release the semaphore
				return;
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		try{
			htableExecutorService.markLastCheckinMs();
			htableExecutorService.purge();
			if(possiblyTarnished){//discard
				logWithPoolInfo("ThreadPoolExecutor possibly tarnished, discarding", tableName);
				DatarouterCounters.incClientTable(clientType, "HTable executor possiblyTarnished", clientId.getName(),
						tableName, 1L);
				htableExecutorService.terminateAndBlockUntilFinished();
			}else if(htableExecutorService.isDyingOrDead(tableName)){//discard
				logWithPoolInfo("ThreadPoolExecutor not reusable, discarding", tableName);
				DatarouterCounters.incClientTable(clientType, "HTable executor isDyingOrDead", clientId.getName(),
						tableName, 1L);
				htableExecutorService.terminateAndBlockUntilFinished();
			}else if(!htableExecutorService.isTaskQueueEmpty()){//discard
				logWithPoolInfo("ThreadPoolExecutor taskQueue not empty, discarding", tableName);
				DatarouterCounters.incClientTable(clientType, "HTable executor taskQueue not empty", clientId.getName(),
						tableName, 1L);
				htableExecutorService.terminateAndBlockUntilFinished();
			}else if(!htableExecutorService.waitForActiveThreadsToSettle(tableName)){//discard
				logWithPoolInfo("active thread count would not settle to 0", tableName);
				DatarouterCounters.incClientTable(clientType, "HTable executor pool active threads won't quit",
						clientId.getName(), tableName, 1L);
				htableExecutorService.terminateAndBlockUntilFinished();
			}else{
				if(executorServiceQueue.offer(htableExecutorService)){//keep it!
					DatarouterCounters.incClientTable(clientType, "connection HTable returned to pool",
							clientId.getName(), tableName, 1L);
				}else{//discard
					logWithPoolInfo("checkIn HTable but queue already full, so close and discard", tableName);
					DatarouterCounters.incClientTable(clientType, "HTable executor pool overflow", clientId.getName(),
							tableName, 1L);
					htableExecutorService.terminateAndBlockUntilFinished();
				}
			}
		}finally{
			releaseSempahoreAndCheckConsistency(tableName);
		}
	}

	public Integer getTotalPoolSize(){
		return executorServiceQueue.size();
	}

	public void shutdown(){
		shuttingDown = true;
		if(htableSemaphoreActivePermits() != 0){
			final int sleepMs = 5000;
			logger.warn("Still {} active tables.  Sleeping {}ms", htableSemaphoreActivePermits(), sleepMs);
			ThreadTool.sleepUnchecked(sleepMs);
		}
		for(HBaseTableExecutorService executorService : executorServiceQueue){
			executorService.terminateAndBlockUntilFinished();
		}
		try{
			connection.close();
		}catch(IOException e){
			logger.warn("", e);
		}
	}

	/*-------------------------- private ------------------------------------*/

	//for some reason, synchronizing this method wreaks havoc and stops all progress
	private /*synchronized*/ void checkConsistencyAndAcquireSempahore(String tableName){
		logIfInconsistentCounts(true, tableName);
		long startAquireMs = System.currentTimeMillis();
		SemaphoreTool.acquire(htableSemaphore);
		long acquireTimeMs = System.currentTimeMillis() - startAquireMs;
		if(acquireTimeMs > LOG_SEMAPHORE_ACQUISITIONS_OVER_MS){
			logger.warn("acquiring semaphore took " + NumberFormatter.addCommas(acquireTimeMs) + "ms");
		}
	}

	private synchronized void releaseSempahoreAndCheckConsistency(String tableName){
		htableSemaphore.release();
		logIfInconsistentCounts(false, tableName);
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

	/*-------------------------- logging ------------------------------------*/

	private void recordSlowCheckout(long checkOutDurationMs, String tableName){
		if(!LOG_ACTIONS){
			return;
		}
		if(checkOutDurationMs > 1){
			DatarouterCounters.incClientTable(clientType, "connection open > 1ms", clientId.getName(), tableName, 1L);
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
			logWithPoolInfo("inconsistent pool counts on " + (checkOut ? "checkOut" : "checkIn"), tableName);
		}
		lastLoggedWarning = System.currentTimeMillis();
	}

	private void logWithPoolInfo(String message, String tableName){
		if(!LOG_ACTIONS){
			return;
		}
		innerLogWithPoolInfo(message, tableName);
	}

	private void innerLogWithPoolInfo(String message, String tableName){
		logger.info(getPoolInfoLogMessage(tableName) + ", " + message);
	}

	private String getPoolInfoLogMessage(String tableName){
		return "max=" + maxHTables
				+ ", blocked=" + htableSemaphore.getQueueLength()
				+ ", idle=" + executorServiceQueue.size()
				+ ", permits=" + htableSemaphoreActivePermits()
				+ ", HTables=" + activeHTables.size()
				+ ", client=" + clientId.getName()
				+ ", table=" + tableName;
	}

}
