package com.hotpads.datarouter.client.imp.hbase.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

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
	protected static Logger logger = Logger.getLogger(HBaseMultiAttemptTask.class);
	
	protected static final Integer DEFAULT_NUM_ATTEMPTS = 3;
	protected static final Long DEFAULT_TIMEOUT_MS = 10 * 1000L;
	
	protected static long 
		throttleEmailsMs = 5 * DateTool.MILLISECONDS_IN_MINUTE,
		lastEmailSentAtMs = 0L;

	protected static final Boolean CANCEL_THREAD_IF_RUNNING = true;
	
	
	/*************************** fields *****************************/
	
	protected DataRouterContext drContext;
		
	protected HBaseTask<V> task;
	protected HBaseClient client;
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
				client = task.node.getClient();
				if(client==null){
					Thread.sleep(timeoutMs);//otherwise will loop through numAttempts as fast as possible
					throw new DataAccessException("client "+this.task.node.getClientName()+" not active"); 
				}
				executorService = client.getExecutorService();
				
				//set retry params
				task.setAttemptNumOneBased(i);//pass these in for Tracing purposes
				task.setNumAttempts(numAttempts);//Tracing
				task.setTimeoutMs(timeoutMs);//Tracing
				Future<V> future = executorService.submit(task);
				try{
					return future.get(timeoutMs, TimeUnit.MILLISECONDS);
				}catch(TimeoutException e){
					future.cancel(CANCEL_THREAD_IF_RUNNING);
					logger.warn("TimeoutException on task with progress="+task.progress);
					throw new DataAccessException(e);
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
		sendThrottledErrorEmail(finalAttempException);
		throw new DataAccessException("timed out "+numAttempts+" times at timeoutMs="+timeoutMs, 
				finalAttempException);
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
	
	protected void sendThrottledErrorEmail(Exception e) {
		boolean enoughTimePassed = System.currentTimeMillis() - lastEmailSentAtMs > throttleEmailsMs;
		if(!enoughTimePassed) { return; }
		String subject = "HBaseMultiAttempTask failure on "+drContext.getServerName();
		String body = "Message throttled for "+throttleEmailsMs+"ms\n\n"+ExceptionTool.getStackTraceAsString(e);
		DataRouterEmailTool.sendEmail("admin@hotpads.com", drContext.getAdministratorEmail(), subject, body);
		lastEmailSentAtMs = System.currentTimeMillis();
	}
}
