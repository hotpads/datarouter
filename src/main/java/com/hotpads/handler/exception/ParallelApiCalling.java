package com.hotpads.handler.exception;

import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_RECIPENT_TYPE_EMAIL;
import static com.hotpads.handler.exception.NotificationApiConstants.SERVER_EXCEPTION_NOTIFICATION_TYPE;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public class ParallelApiCalling {

	private static Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);
	
	private static final String CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL = "cguillaume@hotpads.com";

	private static final long FLUSH_PERIOD_MS = 1000;//second
	private static final long FLUSH_TIMEOUT_MS = 2 * 1000;//millisecond

	private ScheduledExecutorService flusher;
	private ExecutorService sender;
	private Queue<ExceptionRecordAndClass> queue;
	private NotificationApiCaller notificationApiCaller;

	public ParallelApiCalling() {
		queue = new LinkedBlockingQueue<ExceptionRecordAndClass> ();
		ThreadGroup threadGroup = new ThreadGroup("notification");
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(threadGroup, "notification", true);
		flusher = Executors.newScheduledThreadPool(1, namedThreadFactory);
		sender = Executors.newSingleThreadExecutor();
		flusher.scheduleWithFixedDelay(new QueueFlusher(), 0, FLUSH_PERIOD_MS, TimeUnit.MILLISECONDS);
	}

	public void setNotificationApiCaller(NotificationApiCaller notificationApiCaller) {
		this.notificationApiCaller = notificationApiCaller;
	}

	public void add(ExceptionRecord exceptionRecord, Class<? extends Exception> clazz) {
		queue.add(new ExceptionRecordAndClass(exceptionRecord, clazz));
	}

	public class ExceptionRecordAndClass { //We can not use NotificationRequest in this project

		private ExceptionRecord record;

		private Class<? extends Exception> clazz;

		public ExceptionRecordAndClass(ExceptionRecord record, Class<? extends Exception> exception) {
			this.record = record;
			this.clazz = exception;
		}

	}

	private class QueueFlusher implements Runnable {

		@Override
		public void run() {
			while (CollectionTool.notEmpty(queue)) {
				ExceptionRecordAndClass exceptionRecordAndClass = queue.poll();
				Future<Boolean> future = sender.submit(new ApiCallAttempt(exceptionRecordAndClass));
				new FailedTester(future, exceptionRecordAndClass).start();
			}
		}

	}

	private class FailedTester extends Thread {

		private Future<Boolean> future;
		private ExceptionRecordAndClass exceptionRecordAndClass;

		public FailedTester(Future<Boolean> future, ExceptionRecordAndClass exceptionRecordAndClass) {
			this.future = future;
			this.exceptionRecordAndClass = exceptionRecordAndClass;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			System.out.println("start " + start);
			try {
				if (future.get(FLUSH_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
					logger.info("Request terminated in " + (System.currentTimeMillis() - start) + "ms");
					return;
				}
				logger.warn("Request to NotificationApi failed, email will be sent");
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				logger.warn("Request to NotificationApi failed, email will be sent", e);
			}
			//TODO send by classic mail
		}
	}

	private class ApiCallAttempt implements Callable<Boolean> {

		private ExceptionRecordAndClass e;

		public ApiCallAttempt(ExceptionRecordAndClass e) {
			this.e = e;
		}

		@Override
		public Boolean call() throws Exception {
			try {
				notificationApiCaller.call(
						NOTIFICATION_RECIPENT_TYPE_EMAIL,
						CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL, //only for dev
						SERVER_EXCEPTION_NOTIFICATION_TYPE,
						e.record.getKey().getId(),
						e.clazz.getName());
				return true;
			} catch(Exception e) {
				return false;
			}
		}

	}

}
