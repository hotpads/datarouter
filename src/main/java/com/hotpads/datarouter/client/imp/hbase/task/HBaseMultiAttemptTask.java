package com.hotpads.datarouter.client.imp.hbase.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.util.DataRouterEmailTool;
import com.hotpads.trace.TracedCallable;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.ExceptionTool;

//consider forming base class with commonalities from MemcachedMultiAttemptTash
public class HBaseMultiAttemptTask<V> extends TracedCallable<V>{
	protected static Logger logger = LoggerFactory.getLogger(HBaseMultiAttemptTask.class);
	
	protected static final Integer DEFAULT_NUM_ATTEMPTS = 2;
	protected static final Long DEFAULT_TIMEOUT_MS = 3 * 1000L;
	
	protected static long 
		THROTTLE_ERROR_EMAIL_MINUTES = 5,
		THROTTLE_ERROR_EMAIL_MS = THROTTLE_ERROR_EMAIL_MINUTES * DateTool.MILLISECONDS_IN_MINUTE,
		LAST_EMAIL_SENT_AT_MS = 0L;

	protected static final AtomicLong NUM_FAILED_ATTEMPTS_SINCE_LAST_EMAIL = new AtomicLong(0);

	protected static final Boolean CANCEL_THREAD_IF_RUNNING = true;
	
	
	/*************************** fields *****************************/
	
	protected DataRouterContext drContext;
		
	protected HBaseTask<V> task;
	protected ExecutorService executorService;
	protected Config config;
	protected Long timeoutMs;
	protected Integer numAttempts;
	
	
	/************************** constructors ***************************/
	
	public HBaseMultiAttemptTask(HBaseTask<V> task){
		super(HBaseMultiAttemptTask.class.getSimpleName()+"."+task.getTaskName());
		this.drContext = task.getDrContext();
		this.task = task;
		//temp hack.  in case of replaced client, we still use old client's exec service
		this.config = Config.nullSafe(task.config);
		this.timeoutMs = getTimeoutMS(config);
		this.numAttempts = getNumAttempts(config);
	}
	
	@Override
	public V wrappedCall(){
		Exception finalAttempException = null;
		for(int i=1; i <= numAttempts; ++i){
			try{
				//do this client stuff here so inaccessible clients count as normal failures
				HBaseClient client = (HBaseClient)drContext.getClientPool().getClient(task.getClientName());
				if(client==null){
					Thread.sleep(timeoutMs);//otherwise will loop through numAttempts as fast as possible
					throw new DataAccessException("client "+task.getClientName()+" not active"); 
				}
				executorService = client.getExecutorService();
				
				//set retry params
				task.setAttemptNumOneBased(i);//pass these in for Tracing purposes
				task.setNumAttempts(numAttempts);//Tracing
				task.setTimeoutMs(timeoutMs);//Tracing
				Future<V> future = executorService.submit(task);
				try{
//					return future.get(timeoutMs, TimeUnit.MILLISECONDS);
					return future.get();
//				}catch(TimeoutException e){
//					future.cancel(CANCEL_THREAD_IF_RUNNING);
//					logger.warn("TimeoutException on task with progress="+task.progress);
//					throw new DataAccessException(e);
				}catch(InterruptedException e){
					throw new DataAccessException(e);
				}catch(ExecutionException e){
					throw new DataAccessException(e);
				}
			}catch(Exception attemptException){
				finalAttempException = attemptException;
				if(isLastAttempt(i)) {
					logger.warn("attempt "+i+"/"+numAttempts+" failed with the following exception");
					logger.warn(ExceptionTool.getStackTraceAsString(attemptException));
				}else {
					logger.warn("attempt "+i+"/"+numAttempts+" failed, retrying");
				}
			}
		}
		String errorMessage = "errored "+numAttempts+" times.  timeoutMs="+timeoutMs;
		sendThrottledErrorEmail(errorMessage, finalAttempException);
		throw new DataAccessException(finalAttempException);
	}
	
	protected static Long getTimeoutMS(Config config){
		if(config.getTimeoutMs()!=null){ return config.getTimeoutMs(); }
		return DEFAULT_TIMEOUT_MS;
	}
	
	protected static Integer getNumAttempts(Config config){
		if(config==null){ return DEFAULT_NUM_ATTEMPTS; }
		if(config.getNumAttempts()==null){ return DEFAULT_NUM_ATTEMPTS; }
		return config.getNumAttempts();
	}
	
	protected boolean isLastAttempt(int i) {
		return i==numAttempts;
	}
	
	protected void sendThrottledErrorEmail(String timeoutMessage, Exception e) {
		NUM_FAILED_ATTEMPTS_SINCE_LAST_EMAIL.incrementAndGet();
		boolean enoughTimePassed = System.currentTimeMillis() - LAST_EMAIL_SENT_AT_MS > THROTTLE_ERROR_EMAIL_MS;
		long throttleEmailSeconds = THROTTLE_ERROR_EMAIL_MS / 1000;
		if(!enoughTimePassed) { return; }
		long numFailures = NUM_FAILED_ATTEMPTS_SINCE_LAST_EMAIL.get();
		String subject = "HBaseMultiAttempTask failure on "+drContext.getServerName();
		String body = "Message throttled for "+throttleEmailSeconds+" seconds"
				+"\n\n"+timeoutMessage
				+"\n\n"+numFailures+" since last email attempt "+DateTool.getAgoString(LAST_EMAIL_SENT_AT_MS)
				+"\n\n"+ExceptionTool.getStackTraceAsString(e);
		DataRouterEmailTool.sendEmail("admin@hotpads.com", drContext.getAdministratorEmail(), subject, body);
		LAST_EMAIL_SENT_AT_MS = System.currentTimeMillis();
		NUM_FAILED_ATTEMPTS_SINCE_LAST_EMAIL.set(0L);
	}
}
