package com.hotpads.notification;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.exception.ExceptionRecordKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public class ExceptionRecordPersister{
	private static Logger logger = Logger.getLogger(ExceptionRecordPersister.class);

	private static final int QUEUE_CAPACITY = 4096;

	private MapStorage<ExceptionRecordKey,ExceptionRecord> exceptionRecordNode;
	private BlockingQueue<ExceptionRecord> queue;

	private ScheduledExecutorService flushScheduler;
	private ExecutorService flushExecutor;

	public ExceptionRecordPersister(MapStorage<ExceptionRecordKey,ExceptionRecord> exceptionRecordNode) {
		this.exceptionRecordNode = exceptionRecordNode;
		this.queue = new LinkedBlockingDeque<ExceptionRecord>(QUEUE_CAPACITY);
		this.flushScheduler = Executors.newScheduledThreadPool(1);
		this.flushExecutor = Executors.newCachedThreadPool();
		flushScheduler.scheduleWithFixedDelay(new Flusher(), 1000, 1000, TimeUnit.MILLISECONDS);
	}
	
	public boolean addToQueue(ExceptionRecord exceptionRecord) {
		logger.warn("discarding ExceptionRecord because queue is full");
		//TODO should we dump the old entries and keep the newer ones?
		return queue.offer(exceptionRecord);
	}
	
	
	private class Flusher implements Runnable{
		private static final int FLUSH_BATCH_SIZE = 100;
		private static final long RECORD_TIMEOUT_MS = 200;
		
		@Override
		public void run(){
			while(CollectionTool.notEmpty(queue)){
				List<ExceptionRecord> flushBatch = ListTool.createArrayList();
				queue.drainTo(flushBatch, FLUSH_BATCH_SIZE);
				Future<Boolean> future = flushExecutor.submit(new FlushAttempt(flushBatch));
				try {
					future.get(RECORD_TIMEOUT_MS, TimeUnit.MILLISECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private class FlushAttempt implements Callable<Boolean> {
		private List<ExceptionRecord> flushBatch;

		public FlushAttempt(List<ExceptionRecord> flushBatch) {
			this.flushBatch = flushBatch;
		}

		@Override
		public Boolean call() throws Exception {
			try {
				exceptionRecordNode.putMulti(flushBatch, null);
				return true;
			} catch (Exception e) {
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				return false;
			}
		}
	}
}
