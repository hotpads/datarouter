package com.hotpads.handler.exception;

import java.util.List;
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

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public class ParallelApiCalling {

	private static Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);

	private static final long FLUSH_PERIOD_MS = 1000;//second
	private static final long FLUSH_TIMEOUT_MS = 1000;//millisecond

	private ScheduledExecutorService flusher;
	private ExecutorService sender;
	private Queue<NotificationRequest> queue;
	private NotificationApiCaller notificationApiCaller;

	public ParallelApiCalling() {
		queue = new LinkedBlockingQueue<NotificationRequest> ();
		ThreadGroup threadGroup = new ThreadGroup("notification");
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(threadGroup, "notification", true);
		flusher = Executors.newScheduledThreadPool(1, namedThreadFactory);
		sender = Executors.newSingleThreadExecutor();
		flusher.scheduleWithFixedDelay(new QueueFlusher(), 0, FLUSH_PERIOD_MS, TimeUnit.MILLISECONDS);
	}

	public void setNotificationApiCaller(NotificationApiCaller notificationApiCaller) {
		this.notificationApiCaller = notificationApiCaller;
	}

	public void add(NotificationRequest request) {
		queue.add(request);
	}

	private class QueueFlusher implements Runnable {

		private static final int BATCH_SIZE = 100;

		@Override
		public void run() {
			List<NotificationRequest> requests = ListTool.create();
			while (CollectionTool.notEmpty(queue)) {
				if (requests.size() == BATCH_SIZE) {
					Future<Boolean> future = sender.submit(new ApiCallAttempt(requests));
					new FailedTester(future, requests).start();
					requests = ListTool.create();
				}
				requests.add(queue.poll());
			}
			if (CollectionTool.notEmpty(requests)) {
				Future<Boolean> future = sender.submit(new ApiCallAttempt(requests));
				new FailedTester(future, requests).start();
			}
		}

	}

	private class FailedTester extends Thread {

		private Future<Boolean> future;
		private List<NotificationRequest> requests;

		public FailedTester(Future<Boolean> future, List<NotificationRequest> requests) {
			this.future = future;
			this.requests = requests;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			try {
				if (future.get(FLUSH_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
					logger.warn("Request terminated in " + (System.currentTimeMillis() - start) + "ms");
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

		private List<NotificationRequest> requests;

		public ApiCallAttempt(List<NotificationRequest> requests) {
			this.requests = requests;
		}

		@Override
		public Boolean call() throws Exception {
			try {
				notificationApiCaller.call(requests);
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}

	}

	public void warmupApiClient() {
		if (notificationApiCaller != null) {
			notificationApiCaller.warmup();
		}
	}

}
