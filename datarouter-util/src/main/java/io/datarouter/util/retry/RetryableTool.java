/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.retry;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.concurrent.UncheckedInterruptedException;

public class RetryableTool{
	private static final Logger logger = LoggerFactory.getLogger(RetryableTool.class);

	public static <T> T tryNTimesWithBackoffUnchecked(Retryable<T> callable, int numAttempts, long initialBackoffMs,
			boolean logExceptions){
		return tryNTimesWithBackoffUnchecked(callable, numAttempts, initialBackoffMs, logExceptions, $ -> true);
	}

	public static <T> T tryNTimesWithBackoffUnchecked(Retryable<T> callable, int numAttempts, long initialBackoffMs,
			boolean logExceptions, Predicate<T> successCondition){
		long backoffMs = initialBackoffMs;
		int attemptNum = 1;
		for(; attemptNum <= numAttempts && !Thread.interrupted(); ++attemptNum){
			try{
				T result = callable.call();
				if(!successCondition.test(result)){
					throw new Exception("invalid result " + result);
				}
				return result;
			}catch(Exception e){
				if(attemptNum < numAttempts){
					if(logExceptions){
						logger.warn("exception on attempt {}/{}, sleeping {}ms", attemptNum, numAttempts, backoffMs, e);
					}
					ThreadTool.sleepUnchecked(backoffMs);
				}else{
					if(logExceptions){
						logger.error("exception on final attempt {}", attemptNum);
					}
					if(e instanceof RuntimeException){
						throw (RuntimeException)e;
					}
					throw new RuntimeException(e);
				}
			}
			backoffMs = backoffMs * 2 + ThreadLocalRandom.current().nextLong(0, initialBackoffMs);
		}
		throw new UncheckedInterruptedException("interrupted after " + (attemptNum - 1) + " attempt(s)");
	}

}
