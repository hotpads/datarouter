package com.hotpads.datarouter.client.imp.hibernate.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.util.core.concurrent.Retryable;
import com.hotpads.util.core.concurrent.ThreadTool;

public class JdbcRollbackRetryingCallable<T>
implements Retryable<T>{
	private static final Logger logger = LoggerFactory.getLogger(JdbcRollbackRetryingCallable.class);

	
	private final SessionExecutorImpl<T> callable;
	private final int numAttempts;
	private final long initialDoublingBackoffMs;

	
	//TODO accept a callableSupplier for mutable ops
	public JdbcRollbackRetryingCallable(SessionExecutorImpl<T> callable, int numAttempts,
			long initialDoublingBackoffMs){
		this.callable = callable;
		this.numAttempts = numAttempts;
		this.initialDoublingBackoffMs = initialDoublingBackoffMs;
	}


	@Override
	public T call(){
		long backoffMs = initialDoublingBackoffMs;
		for(int attemptNum = 1; attemptNum <= numAttempts; ++attemptNum){
			try{
				return callable.call();
			}catch(javax.persistence.RollbackException e){//this is fragile.  callable must throw this exact exception
				if(attemptNum < numAttempts){
					logger.warn("rollback on attempt {}/{}, sleeping {}ms", attemptNum, numAttempts, backoffMs, e);
					ThreadTool.sleep(backoffMs);
				}else{
					logger.error("rollback on final attempt {}", attemptNum, e);
					Throwable rollbackCause = e.getCause();
					throw new RuntimeException(rollbackCause);
				}
			}
			backoffMs *= 2;
		}
		throw new RuntimeException("shouldn't get here.  for-loop has bug?");
	}
	
}