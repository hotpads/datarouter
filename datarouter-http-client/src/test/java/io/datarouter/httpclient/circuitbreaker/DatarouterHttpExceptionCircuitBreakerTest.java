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

import java.time.Clock;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.response.exception.DatarouterHttpRequestInterruptedException;

public class DatarouterHttpExceptionCircuitBreakerTest{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpExceptionCircuitBreakerTest.class);

	@Test
	public void testStateTransitions(){
		DatarouterHttpExceptionCircuitBreaker<String> circuitBreaker = new DatarouterHttpExceptionCircuitBreaker<>(
				Clock.systemUTC(), 50, Duration.ofMillis(100), 3, "");
		callFailure(circuitBreaker);
		Assert.assertEquals(circuitBreaker.getState(), CircuitBreakerState.CLOSED);
		callFailure(circuitBreaker);
		Assert.assertEquals(circuitBreaker.getState(), CircuitBreakerState.OPEN);
		circuitBreaker.setClock(Clock.offset(Clock.systemUTC(), Duration.ofMillis(110)));
		Assert.assertEquals(circuitBreaker.getState(), CircuitBreakerState.HALF_OPEN);
		callSuccess(circuitBreaker);
		Assert.assertEquals(circuitBreaker.getState(), CircuitBreakerState.CLOSED);
	}

	@Test
	public void testCircuitFailure(){
		DatarouterHttpExceptionCircuitBreaker<String> circuitBreaker = new DatarouterHttpExceptionCircuitBreaker<>(
				Clock.systemUTC(), 50, Duration.ofSeconds(30), 5, "");
		Assert.assertEquals(circuitBreaker.getState(), CircuitBreakerState.CLOSED);
		callFailure(circuitBreaker);
		callFailure(circuitBreaker);
		Assert.assertEquals(circuitBreaker.getState(), CircuitBreakerState.CLOSED);
		callFailure(circuitBreaker);
		Assert.assertEquals(circuitBreaker.getState(), CircuitBreakerState.OPEN);
	}

	private void callFailure(DatarouterHttpExceptionCircuitBreaker<String> circuitBreaker){
		try{
			circuitBreaker.call(() -> {
				throw new DatarouterHttpRequestInterruptedException(null, 0, null);
			});
		}catch(CircuitBreakerException e){
			logger.debug("");
		}
	}

	private void callSuccess(DatarouterHttpExceptionCircuitBreaker<String> circuitBreaker){
		try{
			circuitBreaker.call(() -> "success");
		}catch(CircuitBreakerException e){
			logger.debug("");
		}
	}

}
