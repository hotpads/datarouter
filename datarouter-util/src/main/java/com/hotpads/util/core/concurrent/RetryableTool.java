package com.hotpads.util.core.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryableTool{
	private static final Logger logger = LoggerFactory.getLogger(RetryableTool.class);
	
	
	public static <T> T tryNTimesWithBackoffUnchecked(Retryable<T> callable, final int numAttempts,
			final long initialDoublingBackoffMs){
		long backoffMs = initialDoublingBackoffMs;
		for(int attemptNum = 1; attemptNum <= numAttempts; ++attemptNum){
			try{
				return callable.call();
			}catch (Exception e){
				if(attemptNum < numAttempts){
					logger.warn("exception on attempt {}/{}, sleeping {}ms", attemptNum, numAttempts, backoffMs, e);
					ThreadTool.sleep(backoffMs);
				}else{
					logger.error("exception on final attempt {}", attemptNum, e);
					throw new RuntimeException(e);
				}
			}
			backoffMs *= 2;
		}
		throw new RuntimeException("shouldn't get here");
	}
	
}
