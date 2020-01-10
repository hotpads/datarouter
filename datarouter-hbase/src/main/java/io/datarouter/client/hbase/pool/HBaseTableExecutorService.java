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

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.concurrent.ThreadTool;

/*
 * The HBase client uses one thread per regionserver per HTable.  This wrapper class stores a java ExecSvc with 1
 * thread per regionserver.  It can be reused across different tables.
 */
public class HBaseTableExecutorService{
	private static final Logger logger = LoggerFactory.getLogger(HBaseTableExecutorService.class);

	private static final long TIMEOUT_MS = 10 * 1000L;
	private static final AtomicInteger executorServiceNumber = new AtomicInteger(1);

	private final ThreadPoolExecutor exec;
	private final long createdMs;
	private volatile long lastCheckInMs;

	public HBaseTableExecutorService(int minThreads, int maxThreads){
		//it's important to use a bounded queue as the executor service won't grow past minThreads until you fill the
		// queue.  SynchronousQueue is a special zero size queue that will cause the exec svc to grow immediately
		BlockingQueue<Runnable> queue = new SynchronousQueue<>();
		ThreadFactory threadFactory = new NamedThreadFactory("htable-" + executorServiceNumber.incrementAndGet(),
				false);
		//having more regionservers than maxThreads will cause a RejectedExecutionException that will kill your hbase
		// request, so provide a high maxThreads
		this.exec = new ThreadPoolExecutor(minThreads, maxThreads, 60, TimeUnit.SECONDS, queue, threadFactory);
		this.exec.allowCoreThreadTimeOut(true);// see class comment regarding killing pools
		this.createdMs = System.currentTimeMillis();
		this.lastCheckInMs = createdMs;
	}

	public void markLastCheckinMs(){
		lastCheckInMs = System.currentTimeMillis();
	}

	public boolean isExpired(){
		long elapsedMs = System.currentTimeMillis() - lastCheckInMs;
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

	public void terminateAndBlockUntilFinished(){
		ExecutorServiceTool.shutdown(exec, Duration.ofDays(1));
	}

	public ThreadPoolExecutor getExec(){
		return exec;
	}

}
