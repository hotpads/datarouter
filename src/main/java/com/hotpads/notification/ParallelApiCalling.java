package com.hotpads.notification;

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

	private static Logger logger = Logger.getLogger(ParallelApiCalling.class);

	private static final long FLUSH_PERIOD_MS = 1000;//second
	private static final long FLUSH_TIMEOUT_MS = 1000;//millisecond

	private ScheduledExecutorService flusher;
	private ExecutorService sender;
	private Queue<NotificationRequest> queue;
	private NotificationApiClient notificationApiClient;

	private boolean premier;

	public ParallelApiCalling(NotificationApiClient notificationApiClient) {
		this.notificationApiClient = notificationApiClient;
		this.queue = new LinkedBlockingQueue<NotificationRequest> ();
		ThreadGroup threadGroup = new ThreadGroup("notification");
		NamedThreadFactory namedThreadFactory = new NamedThreadFactory(threadGroup, "notification", true);
		this.flusher = Executors.newScheduledThreadPool(1, namedThreadFactory);
		this.sender = Executors.newSingleThreadExecutor();
		this.flusher.scheduleWithFixedDelay(new QueueFlusher(), 0, FLUSH_PERIOD_MS, TimeUnit.MILLISECONDS);
		this.premier = true;
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
					new FailedTester(future, requests, premier).start();
					premier = false;
					requests = ListTool.create();
				}
				requests.add(queue.poll());
			}
			if (CollectionTool.notEmpty(requests)) {
				Future<Boolean> future = sender.submit(new ApiCallAttempt(requests));
				new FailedTester(future, requests, premier).start();
				premier = false;
			}
		}

	}

	private class FailedTester extends Thread {

		private Future<Boolean> future;
		private List<NotificationRequest> requests;
		private long coef;

		public FailedTester(Future<Boolean> future, List<NotificationRequest> requests, boolean premier) {
			this.future = future;
			this.requests = requests;
			this.coef = premier ? 2l : 1l;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			try {
				if (future.get(coef * FLUSH_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
					logger.log("Request terminated in " + (System.currentTimeMillis() - start) + "ms");
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
				notificationApiClient.call(requests);
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}

	}

	public void warmupApiClient() {
		notificationApiClient.warmup();
	}

}
