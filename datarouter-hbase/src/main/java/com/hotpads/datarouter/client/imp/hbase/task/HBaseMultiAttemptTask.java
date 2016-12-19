package com.hotpads.datarouter.client.imp.hbase.task;

import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.DatarouterEmailTool;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.trace.TracedCallable;
import com.hotpads.util.DrExceptionTool;

//consider forming base class with commonalities from MemcachedMultiAttemptTash
public class HBaseMultiAttemptTask<V>extends TracedCallable<V>{
	private static final Logger logger = LoggerFactory.getLogger(HBaseMultiAttemptTask.class);

	private static final int DEFAULT_MAX_ATTEMPTS = 2;
	private static final long DEFAULT_TIMEOUT_MS = 3 * 1000;

	private static final long
		THROTTLE_ERROR_EMAIL_MINUTES = 5,
		THROTTLE_ERROR_EMAIL_MS = THROTTLE_ERROR_EMAIL_MINUTES * DrDateTool.MILLISECONDS_IN_MINUTE;

	private static long LAST_EMAIL_SENT_AT_MS = 0;

	private static final AtomicLong NUM_FAILED_ATTEMPTS_SINCE_LAST_EMAIL = new AtomicLong(0);

	/*************************** fields *****************************/

	private final Datarouter datarouter;

	private final HBaseTask<V> task;
	private ExecutorService executorService;
	private long timeoutMs;
	private int maxAttempts;

	/************************** constructors ***************************/

	public HBaseMultiAttemptTask(HBaseTask<V> task){
		super(HBaseMultiAttemptTask.class.getSimpleName() + "." + task.getTaskName());
		this.datarouter = task.getDatarouter();
		this.task = task;
		// temp hack. in case of replaced client, we still use old client's exec service
		Config config = Config.nullSafe(task.config);
		this.timeoutMs = getTimeoutMs(config);
		this.maxAttempts = getMaxAttempts(config);
	}

	@Override
	public V wrappedCall(){
		for(int i = 1; i <= maxAttempts; ++i){
			try{
				// do this client stuff here so inaccessible clients count as normal failures
				HBaseClient client = (HBaseClient)datarouter.getClientPool().getClient(task.getClientName());
				if(client == null){
					Thread.sleep(timeoutMs);// otherwise will loop through numAttempts as fast as possible
					throw new DataAccessException("client " + task.getClientName() + " not active");
				}
				executorService = client.getExecutorService();

				// set retry params
				task.setAttemptNumOneBased(i);// pass these in for Tracing purposes
				task.setNumAttempts(maxAttempts);// Tracing
				task.setTimeoutMs(timeoutMs);// Tracing
				Future<V> future = executorService.submit(task);
				try{
					return future.get();
				}catch(ExecutionException e){
					throwIfInterruped(e);
					logger.warn("rethrowing ExecutionException as DataAccessException", e);
					throw new DataAccessException(e);
				}
			}catch(Exception attemptException){
				throwIfInterruped(attemptException);
				if(isLastAttempt(i)){
					logger.warn("attempt " + i + "/" + maxAttempts + " failed", attemptException);
					String errorMessage = "errored " + maxAttempts + " times.  timeoutMs=" + timeoutMs;
					sendThrottledErrorEmail(errorMessage, attemptException);
					throw new DataAccessException(attemptException);
				}
				logger.warn("attempt " + i + "/" + maxAttempts + " failed, retrying");
			}
		}
		return null; // Cannot be here
	}

	private void throwIfInterruped(Throwable throwable){
		do{
			if(throwable instanceof InterruptedException || throwable instanceof ClosedByInterruptException){
				throw new DataAccessException(throwable);
			}
			throwable = throwable.getCause();
		}while(throwable != null);
	}

	private static long getTimeoutMs(Config config){
		if(config.getTimeoutMs() != null){
			return config.getTimeoutMs();
		}
		return DEFAULT_TIMEOUT_MS;
	}

	private static int getMaxAttempts(Config config){
		if(config.getNumAttempts() == null){
			return DEFAULT_MAX_ATTEMPTS;
		}
		return config.getNumAttempts();
	}

	private boolean isLastAttempt(int numAttempts){
		return numAttempts == maxAttempts;
	}

	private void sendThrottledErrorEmail(String timeoutMessage, Exception exception){
		NUM_FAILED_ATTEMPTS_SINCE_LAST_EMAIL.incrementAndGet();
		boolean enoughTimePassed = System.currentTimeMillis() - LAST_EMAIL_SENT_AT_MS > THROTTLE_ERROR_EMAIL_MS;
		long throttleEmailSeconds = THROTTLE_ERROR_EMAIL_MS / 1000;
		if(!enoughTimePassed){
			return;
		}
		long numFailures = NUM_FAILED_ATTEMPTS_SINCE_LAST_EMAIL.get();
		String subject = "HBaseMultiAttempTask failure on " + datarouter.getServerName();
		String body = "Message throttled for " + throttleEmailSeconds + " seconds"
				+ "\n\n" + timeoutMessage
				+ "\n\n" + numFailures + " since last email attempt " + DrDateTool.getAgoString(LAST_EMAIL_SENT_AT_MS)
				+ "\n\n" + DrExceptionTool.getStackTraceAsString(exception);
		DatarouterEmailTool.trySendEmail("noreply@hotpads.com", datarouter.getAdministratorEmail(), subject, body);
		LAST_EMAIL_SENT_AT_MS = System.currentTimeMillis();
		NUM_FAILED_ATTEMPTS_SINCE_LAST_EMAIL.set(0L);
	}

}
