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
package io.datarouter.httpclient.circuitbreaker;

import static io.datarouter.httpclient.circuitbreaker.CircuitBreakerState.HALF_OPEN;
import static io.datarouter.httpclient.circuitbreaker.CircuitBreakerState.OPEN;

import java.time.Clock;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRuntimeException;

public class DatarouterHttpExceptionCircuitBreaker<T> extends ExceptionCircuitBreaker{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpExceptionCircuitBreaker.class);

	public DatarouterHttpExceptionCircuitBreaker(Clock clock, int failurePercentageThreshold, Duration retryTime,
			int callResultQueueSize,String name){
		super(clock, failurePercentageThreshold, retryTime, callResultQueueSize, name);
	}

	public DatarouterHttpExceptionCircuitBreaker(String name){
		super(name);
	}

	public T call(CheckedSupplier<T, DatarouterHttpException> call) throws CircuitBreakerException{
		CircuitBreakerState state = getState();
		if(state == OPEN){
			incrementCounterOnStateChange("open");
			logger.error("Circuit opened. CircuitName={}", name);
			throw new CircuitBreakerException("Circuit open");
		}

		try{
			T result = call.get();
			if(state == HALF_OPEN){
				callResultQueue.reset();
				incrementCounterOnStateChange("closing");
				logger.error("Half opened circuit now closing. CircuitName={}",name);
			}
			callResultQueue.insertResult(true);
			return result;
		}catch(DatarouterHttpException | DatarouterHttpRuntimeException e){
			callResultQueue.insertResult(false);
			throw new CircuitBreakerException(e);
		}
	}
}
