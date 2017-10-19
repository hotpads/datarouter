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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.exception.InterruptedRuntimeException;

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
		Thread.currentThread().interrupt();
		throw new InterruptedRuntimeException();
	}

}
