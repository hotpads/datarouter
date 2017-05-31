package com.hotpads.datarouter.client.imp.mysql.execution;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorPleaseRetryException;
import com.hotpads.util.core.concurrent.Retryable;
import com.hotpads.util.core.concurrent.ThreadTool;

public class JdbcRollbackRetryingCallable<T>
implements Retryable<T>{
	private static final Logger logger = LoggerFactory.getLogger(JdbcRollbackRetryingCallable.class);


	private final SessionExecutorImpl<T> callable;
	private final int numAttempts;
	private final long initialBackoffMs;


	//TODO accept a callableSupplier for mutable ops
	public JdbcRollbackRetryingCallable(SessionExecutorImpl<T> callable, int numAttempts, long initialBackoffMs){
		this.callable = callable;
		this.numAttempts = numAttempts;
		this.initialBackoffMs = initialBackoffMs;
	}


	@Override
	public T call(){
		long backoffMs = initialBackoffMs;
		for(int attemptNum = 1; attemptNum <= numAttempts; ++attemptNum){
			try{
				return callable.call();
			}catch(SessionExecutorPleaseRetryException e){//fragile; SessionExecutorImpl must throw this exact exception
				if(attemptNum < numAttempts){
					logger.warn("rollback on attempt {}/{}, sleeping {}ms", attemptNum, numAttempts, backoffMs, e);
					ThreadTool.sleep(backoffMs);
				}else{
					logger.error("rollback on final attempt {}", attemptNum, e);
					Throwable rollbackCause = e.getCause();
					throw new RuntimeException(rollbackCause);
				}
			}
			backoffMs = (backoffMs * 2) + ThreadLocalRandom.current().nextLong(0, initialBackoffMs);
		}
		throw new RuntimeException("shouldn't get here.  for-loop has bug?");
	}
}