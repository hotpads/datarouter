package com.hotpads.notification;

import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.hotpads.datarouter.util.DataRouterEmailTool;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class ParallelApiCaller {
	private static Logger logger = Logger.getLogger(ParallelApiCaller.class);

	private static final int QUEUE_CAPACITY = 4096;
	private static final long FLUSH_PERIOD_MS = 1000;
	private static final long FLUSH_TIMEOUT_MS = 1000;

	private ScheduledExecutorService flusher;
	private ExecutorService sender;
	private Queue<NotificationRequest> queue;
	private NotificationApiClient notificationApiClient;

	private boolean premier;

	public ParallelApiCaller(NotificationApiClient notificationApiClient) {
		this.notificationApiClient = notificationApiClient;
		this.queue = new LinkedBlockingQueue<NotificationRequest>(QUEUE_CAPACITY);
		this.sender = Executors.newSingleThreadExecutor(); //singleThread
		this.flusher = Executors.newScheduledThreadPool(1); //singleThread
		this.flusher.scheduleWithFixedDelay(new QueueFlusher(), 0, FLUSH_PERIOD_MS, TimeUnit.MILLISECONDS);
		this.premier = true;
	}

	public void add(NotificationRequest request){
		logger.warn("discarding NotificationRequest because queue is full");
		//TODO should we dump the old entries and keep the newer ones?
		queue.offer(request);
	}

	
	private class QueueFlusher implements Runnable {
		private static final int BATCH_SIZE = 100;

		@Override
		public void run() {
			List<NotificationRequest> requests = ListTool.createArrayList();
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

	
	private static class FailedTester extends Thread {
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
					logger.info("Request terminated in " + (System.currentTimeMillis() - start) + "ms");
					return;
				}
				logger.warn("Request to NotificationApi failed, email will be sent");
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				logger.warn("Request to NotificationApi failed, email will be sent", e);
			}
			sendEmail(requests);
		}
	}

	
	private class ApiCallAttempt implements Callable<Boolean> {
		private List<NotificationRequest> requests;

		public ApiCallAttempt(List<NotificationRequest> requests) {
			this.requests = requests;
		}

		@Override
		public Boolean call() {
			try {
				notificationApiClient.call(requests);
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}

	}

	public static void sendEmail(List<NotificationRequest> requests) {
		String recipient = requests.get(0).getKey().getUserId();
		String fromEmail = "HotPads<notifications@hotpads.com>";
		String subject = "Error notification";
		StringBuilder builder = new StringBuilder();
		builder.append("<h1>" + requests.size() + " error" + (requests.size() > 1 ? "s" : "") + " occurred </h1>");
		builder.append("<h2>You receive this e-mail because Job server does not respond on time</h2>");
		for (NotificationRequest r : requests) {
			builder.append("<p>");
				builder.append("<span style=\"color: red;font-weight: bold;\">");
					builder.append(r.getChannel());
				builder.append("</span> at ");
				builder.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(new Date(r.getKey().getSentAtMs())));
			builder.append("</p>");
		}
		DataRouterEmailTool.trySendHtmlEmail(fromEmail, recipient, subject, builder.toString());
	}

}
