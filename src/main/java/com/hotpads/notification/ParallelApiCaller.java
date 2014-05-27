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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.util.DataRouterEmailTool;
import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.setting.NotificationSettings;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Pair;

@Singleton
public class ParallelApiCaller {
	private static Logger logger = Logger.getLogger(ParallelApiCaller.class);

	private static final int QUEUE_CAPACITY = 4096;
	private static final long FLUSH_PERIOD_MS = 1000;
	private static final long FLUSH_TIMEOUT_MS = 1000;

	private ScheduledExecutorService flusher;
	private ExecutorService sender;
	private Queue<Pair<NotificationRequest, ExceptionRecord>> queue;
	private NotificationApiClient notificationApiClient;
	private NotificationSettings notificationSettings;
	private Boolean last;

	@Inject
	public ParallelApiCaller(NotificationApiClient notificationApiClient, NotificationSettings notificationSettings, ExceptionHandlingConfig exceptionHandlingConfig) {
		this.notificationApiClient = notificationApiClient;
		this.queue = new LinkedBlockingQueue<Pair<NotificationRequest, ExceptionRecord>>(QUEUE_CAPACITY);
		this.sender = Executors.newSingleThreadExecutor(); //singleThread
		this.flusher = Executors.newScheduledThreadPool(1); //singleThread
		this.flusher.scheduleWithFixedDelay(new QueueFlusher(exceptionHandlingConfig), 0, FLUSH_PERIOD_MS, TimeUnit.MILLISECONDS);
		this.notificationSettings = notificationSettings;
	}

	public void add(NotificationRequest request, ExceptionRecord exceptionRecord){
		queue.offer(new Pair<NotificationRequest, ExceptionRecord>(request, exceptionRecord));
	}

	
	private class QueueFlusher implements Runnable {
		private static final int BATCH_SIZE = 100;
		private ExceptionHandlingConfig exceptionHandlingConfig;

		public QueueFlusher(ExceptionHandlingConfig exceptionHandlingConfig) {
			this.exceptionHandlingConfig = exceptionHandlingConfig;
		}

		@Override
		public void run() {
			List<Pair<NotificationRequest, ExceptionRecord>> requests = ListTool.createArrayList();
			while (CollectionTool.notEmpty(queue)) {
				if (requests.size() == BATCH_SIZE) {
					Future<Boolean> future = sender.submit(new ApiCallAttempt(requests));
					new FailedTester(future, requests, getCoef(), exceptionHandlingConfig).start();
					requests = ListTool.create();
				}
				requests.add(queue.poll());
			}
			if (CollectionTool.notEmpty(requests)) {
				Future<Boolean> future = sender.submit(new ApiCallAttempt(requests));
				new FailedTester(future, requests, getCoef(), exceptionHandlingConfig).start();
			}
		}

	}

	/**
	 * double the timeout when the httpclient need to be rebuild
	 * @return
	 */
	private long getCoef() {
		if (last == null || last != notificationSettings.getIgnoreSsl().getValue()) {
			last = notificationSettings.getIgnoreSsl().getValue();
			return 2l;
		} else {
			return 1l;
		}
	}

	private static class FailedTester extends Thread {
		private Future<Boolean> future;
		private List<Pair<NotificationRequest, ExceptionRecord>> requests;
		private long coef;
		private ExceptionHandlingConfig exceptionHandlingConfig;

		public FailedTester(Future<Boolean> future, List<Pair<NotificationRequest, ExceptionRecord>> requests, long coef, ExceptionHandlingConfig exceptionHandlingConfig) {
			this.future = future;
			this.requests = requests;
			this.coef = coef;
			this.exceptionHandlingConfig = exceptionHandlingConfig;
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

		private void sendEmail(List<Pair<NotificationRequest, ExceptionRecord>> requests) {
			String domain = exceptionHandlingConfig.isDevServer() ? "localhost:8443" : "hotpads.com";
			String recipient = requests.get(0).getLeft().getKey().getUserId();
			String fromEmail = "HotPads Errors<admin@hotpads.com>";
			String object = requests.get(0).getRight() != null ? "ERROR : " : "";
			String subject = "(EMERGENCY notification) " + object + requests.get(0).getLeft().getChannel();
			StringBuilder builder = new StringBuilder();
			builder.append("<h1>" + requests.size() + " error" + (requests.size() > 1 ? "s" : "") + " occurred </h1>");
			builder.append("<h2>You receive this e-mail because Job server does not respond on time</h2>");
			if (requests.get(0).getRight() == null) {
				builder.append("<p>Type : ");
				builder.append(requests.get(0).getLeft().getType());
				builder.append("</p>");
				builder.append("<p>Channel : ");
				builder.append(requests.get(0).getLeft().getChannel());
				builder.append("</p>");
			}
			for (Pair<NotificationRequest, ExceptionRecord> r : requests) {
				builder.append("<p>");
				builder.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(new Date(r.getLeft().getKey().getSentAtMs())));
				if (r.getRight() != null) {
					builder.append(" on ");
					builder.append(r.getRight().getServerName());
					builder.append("</p>");
					builder.append("<p>");
					builder.append("<a href=\"https://" + domain + "/analytics/exception/details?exceptionRecord=" + r.getRight().getKey().getId() + "\">Details</a>");
					builder.append("</p>");
					builder.append("<pre>");
					builder.append(ExceptionTool.getColorized(r.getRight().getStackTrace()));
					builder.append("</pre>");
				} else {
					builder.append("</p>");
					builder.append("<p>Data : ");
					builder.append(requests.get(0).getLeft().getData());
					builder.append("</p>");
				}
			}
			DataRouterEmailTool.trySendHtmlEmail(fromEmail, recipient, subject, builder.toString());
		}

	}

	
	private class ApiCallAttempt implements Callable<Boolean> {
		private List<Pair<NotificationRequest, ExceptionRecord>> requests;

		public ApiCallAttempt(List<Pair<NotificationRequest, ExceptionRecord>> requests) {
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

}
