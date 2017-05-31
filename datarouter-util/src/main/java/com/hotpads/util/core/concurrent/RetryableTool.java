package com.hotpads.util.core.concurrent;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryableTool{
	private static final Logger logger = LoggerFactory.getLogger(RetryableTool.class);


	public static <T> T tryNTimesWithBackoffUnchecked(Retryable<T> callable, final int numAttempts,
			final long initialBackoffMs){
		long backoffMs = initialBackoffMs;
		for(int attemptNum = 1; attemptNum <= numAttempts && !Thread.interrupted(); ++attemptNum){
			try{
				return callable.call();
			}catch(Exception e){
				if(attemptNum < numAttempts){
					logger.warn("exception on attempt {}/{}, sleeping {}ms", attemptNum, numAttempts, backoffMs, e);
					ThreadTool.sleep(backoffMs);
				}else{
					logger.error("exception on final attempt {}", attemptNum, e);
					if(e instanceof RuntimeException){
						throw (RuntimeException)e;
					}
					throw new RuntimeException(e);
				}
			}
			backoffMs = backoffMs * 2 + ThreadLocalRandom.current().nextLong(0, initialBackoffMs);
		}
		throw new RuntimeException("shouldn't get here");
	}

}
