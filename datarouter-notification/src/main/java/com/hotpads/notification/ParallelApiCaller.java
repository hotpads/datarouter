package com.hotpads.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.util.DatarouterEmailTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrExceptionTool;
import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.http.security.UrlScheme;

@Singleton
public class ParallelApiCaller {
	private static final Logger logger = LoggerFactory.getLogger(ParallelApiCaller.class);

	private static final int QUEUE_CAPACITY = 4096;
	private static final long FLUSH_PERIOD_MS = 1000;
	private static final long FLUSH_TIMEOUT_MS = 1000;

	private static final long RECONNECTION_TIMEOUT_COEF = 4;

	private ScheduledExecutorService flusher;
	private ExecutorService sender;
	private Queue<Pair<NotificationRequest, ExceptionRecord>> queue;
	private NotificationApiClient notificationApiClient;
	private boolean alreadyConnected;

	@Inject
	public ParallelApiCaller(NotificationApiClient notificationApiClient,
			ExceptionHandlingConfig exceptionHandlingConfig,
			@Named(DatarouterExecutorGuiceModule.POOL_parallelApiCallerFlusher) ScheduledExecutorService flusher,
			@Named(DatarouterExecutorGuiceModule.POOL_parallelApiCallerSender) ExecutorService sender) {
		this.notificationApiClient = notificationApiClient;
		this.queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
		this.sender = sender;
		this.flusher = flusher;
		this.flusher.scheduleWithFixedDelay(new QueueFlusher(exceptionHandlingConfig), 0, FLUSH_PERIOD_MS,
				TimeUnit.MILLISECONDS);
	}

	public void add(NotificationRequest request){
		add(request, null);
	}

	public void add(NotificationRequest request, ExceptionRecord exceptionRecord){
		logger.info("Adding {} to queue", request);
		try{
			queue.add(new Pair<>(request, exceptionRecord));
		}catch(Exception e){
			logger.warn("", e);
		}
	}

	private class QueueFlusher implements Runnable {
		private static final int BATCH_SIZE = 100;
		private ExceptionHandlingConfig exceptionHandlingConfig;

		public QueueFlusher(ExceptionHandlingConfig exceptionHandlingConfig) {
			this.exceptionHandlingConfig = exceptionHandlingConfig;
		}

		@Override
		public void run() {
			List<Pair<NotificationRequest, ExceptionRecord>> requests = new ArrayList<>();
			while (DrCollectionTool.notEmpty(queue)) {
				if (requests.size() == BATCH_SIZE) {
					logger.info("Submiting api call attempt with {} notification requet(s)", requests.size());
					Future<Boolean> future = sender.submit(new ApiCallAttempt(requests));
					List<Pair<NotificationRequest,ExceptionRecord>> errorRequests = new LinkedList<>();
					for(Pair<NotificationRequest,ExceptionRecord> request : requests){
						if(request.getRight() != null){
							errorRequests.add(request);
						}
					}
					if(errorRequests.size() > 0){
						new FailedTester(future, requests, getTimeoutMs(), exceptionHandlingConfig).start();
					}
					requests = new ArrayList<>();
				}
				requests.add(queue.poll());
			}
			if (DrCollectionTool.notEmpty(requests)) {
				logger.info("Submiting api call attempt with {} notification requet(s)", requests.size());
				Future<Boolean> future = sender.submit(new ApiCallAttempt(requests));
				new FailedTester(future, requests, getTimeoutMs(), exceptionHandlingConfig).start();
			}
			logger.debug("Notification API client queue size is now {}", queue.size());
		}

	}

	/*
	 * double the timeout when the httpclient need to be rebuild and need to re-established the connection
	 */
	private long getTimeoutMs(){
		if(!alreadyConnected){
			alreadyConnected = true;
			return RECONNECTION_TIMEOUT_COEF * FLUSH_TIMEOUT_MS;
		}
		return FLUSH_TIMEOUT_MS;
	}

	private static class FailedTester extends Thread{
		private Future<Boolean> future;
		private List<Pair<NotificationRequest, ExceptionRecord>> requests;
		private long timeoutMs;
		private ExceptionHandlingConfig exceptionHandlingConfig;

		public FailedTester(Future<Boolean> future, List<Pair<NotificationRequest,ExceptionRecord>> requests,
				long timeoutMs, ExceptionHandlingConfig exceptionHandlingConfig){
			this.future = future;
			this.requests = requests;
			this.timeoutMs = timeoutMs;
			this.exceptionHandlingConfig = exceptionHandlingConfig;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			try {
				if (future.get(timeoutMs, TimeUnit.MILLISECONDS)) {
					logger.info("Request terminated in " + (System.currentTimeMillis() - start) + "ms");
					return;
				}
				logger.warn("Request to NotificationApi failed");
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				logger.warn("Request to NotificationApi failed", e);
			}
			sendEmail(requests);
		}

		private void sendEmail(List<Pair<NotificationRequest, ExceptionRecord>> requests){
			Optional<Pair<NotificationRequest,ExceptionRecord>> optionalPair = requests.stream()
					.filter(pair -> pair.getRight() != null)
					.findAny();
			if(! optionalPair.isPresent()){
				return;
			}
			logger.info("An emergency email will be sent");
			Pair<NotificationRequest,ExceptionRecord> pair = optionalPair.get();
			String domain = exceptionHandlingConfig.isDevServer() ? UrlScheme.LOCAL_DEV_SERVER_HTTPS
					: UrlScheme.DOMAIN_NAME;
			String recipient = pair.getLeft().getKey().getUserId();
			String fromEmail = "noreply@hotpads.com";
			String object = pair.getRight() != null ? "ERROR : " : "";
			String subject = "(EMERGENCY notification) " + object + pair.getLeft().getChannel();
			StringBuilder builder = new StringBuilder();
			builder.append("<h1>" + requests.size() + " error" + (requests.size() > 1 ? "s" : "") + " occurred </h1>");
			builder.append("<h2>You received this direct e-mail because the notification service did not respond in "
					+ "time.</h2>");
			builder.append("<p>Type : ");
			builder.append(pair.getLeft().getType());
			builder.append("</p>");
			builder.append("<p>Channel : ");
			builder.append(pair.getLeft().getChannel());
			builder.append("</p>");
			for (Pair<NotificationRequest, ExceptionRecord> r : requests) {
				if(r.getRight() == null){
					continue;
				}
				builder.append("<p>");
				builder.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(new Date(r.getLeft().getKey()
						.getSentAtMs())));
				builder.append(" on ");
				builder.append(r.getRight().getServerName());
				builder.append("</p>");
				builder.append("<p>");
				builder.append("<a href=\"https://" + domain + "/analytics/exception/details?exceptionRecord=" + r
						.getRight().getKey().getId() + "\">Details</a>");
				builder.append("</p>");
				builder.append("<pre>");
				builder.append(DrExceptionTool.getColorized(r.getRight().getStackTrace()));
				builder.append("</pre>");
			}
			DatarouterEmailTool.trySendHtmlEmail(fromEmail, recipient, subject, builder.toString());
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
				logger.error("",e);
				return false;
			}
		}

	}

}