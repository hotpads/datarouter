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
import com.hotpads.trace.TracedCallable;
import com.hotpads.util.core.ExceptionTool;

public class HBaseMultiAttemptTask<V> extends TracedCallable<V>{
	static Logger logger = Logger.getLogger(HBaseMultiAttemptTask.class);

	protected static final Boolean CANCEL_THREAD_IF_RUNNING = true;
		
	protected HBaseTask<V> task;
	protected HBaseClient client;
	protected ExecutorService executorService;
	protected Config config;
	protected Long timeoutMs;
	protected Integer numAttempts;
	
	public HBaseMultiAttemptTask(HBaseTask<V> task){
		super(HBaseMultiAttemptTask.class.getSimpleName()+"."+task.getTaskName());
		this.task = task;
		//temp hack.  in case of replaced client, we still use old client's exec service
		this.config = Config.nullSafe(task.config);
		this.timeoutMs = getTimeoutMS(this.config);
		this.numAttempts = this.config.getNumAttempts();
		
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
					throw new DataAccessException(e);
				}catch(InterruptedException e){
					throw new DataAccessException(e);
				}catch(ExecutionException e){
					throw new DataAccessException(e);
				}
			}catch(Exception attemptException){
				finalAttempException = attemptException;
				logger.warn("attempt "+i+"/"+numAttempts+" failed with the following exception");
				logger.warn(ExceptionTool.getStackTraceAsString(attemptException));
			}
		}
		throw new DataAccessException("timed out "+numAttempts+" times at timeoutMs="+timeoutMs, 
				finalAttempException);
	}
	
	protected static Long getTimeoutMS(Config config){
		if(config.getTimeoutMs()!=null){ return config.getTimeoutMs(); }
		return HBaseClient.DEFAULT_TIMEOUT_MS;
	}
}