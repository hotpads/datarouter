package com.hotpads.datarouter.client.imp.hbase;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.ExceptionTool;

public class HBaseMultiAttemptTask<V> implements Callable<V>{
	static Logger logger = Logger.getLogger(HBaseMultiAttemptTask.class);

	protected static final Boolean CANCEL_THREAD_IF_RUNNING = true;
	
	protected HBaseTask<V> task;
	protected ExecutorService executorService;
	protected Config config;
	protected Long timeoutMs;
	protected Integer numAttempts;
	
	public HBaseMultiAttemptTask(HBaseTask<V> task){
		this.task = task;
		this.executorService = this.task.client.getExecutorService();
		this.config = Config.nullSafe(task.config);
		this.timeoutMs = this.config.getTimeoutMs();
		this.numAttempts = this.config.getNumAttempts();
		
	}
	
	public V call(){
		for(int i=1; i <= numAttempts; ++i){
			try{
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
			}catch(DataAccessException attemptException){
				logger.warn("attempt "+i+"/"+numAttempts+" failed with the following exception");
				logger.warn(ExceptionTool.getStackTraceAsString(attemptException));
			}
		}
		throw new DataAccessException("timed out "+numAttempts+" times at timeoutMs="+timeoutMs);
	}
}
