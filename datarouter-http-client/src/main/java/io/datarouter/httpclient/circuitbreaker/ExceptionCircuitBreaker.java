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

import static io.datarouter.httpclient.circuitbreaker.CircuitBreakerState.CLOSED;
import static io.datarouter.httpclient.circuitbreaker.CircuitBreakerState.HALF_OPEN;
import static io.datarouter.httpclient.circuitbreaker.CircuitBreakerState.OPEN;

import java.time.Clock;
import java.time.Duration;

import io.datarouter.instrumentation.count.Counters;

public abstract class ExceptionCircuitBreaker{

	protected final String name;
	private final int failurePercentageThreshold;
	private final Duration retryTime;
	protected final CallResultQueue callResultQueue;
	private Clock clock;

	public ExceptionCircuitBreaker(Clock clock, int failurePercentageThreshold, Duration retryTime,
			int callResultQueueSize, String name){
		if(failurePercentageThreshold < 1 || failurePercentageThreshold > 100){
			throw new IllegalArgumentException("Threshold must be between 1-100");
		}
		this.failurePercentageThreshold = failurePercentageThreshold;
		this.retryTime = retryTime;
		this.callResultQueue = new CallResultQueue(clock, callResultQueueSize);
		this.name = name;
		this.clock = clock;
	}

	public ExceptionCircuitBreaker(String name){
		this(Clock.systemUTC(), 75, Duration.ofSeconds(30), 50, name);
	}

	public CircuitBreakerState getState(){
		long now = clock.millis();
		boolean circuitFailure = callResultQueue.getFailurePercentage() > failurePercentageThreshold;
		if(!circuitFailure){
			return CLOSED;
		}

		if(callResultQueue.lastFailureEpochMillis.isPresent() && callResultQueue.lastFailureEpochMillis.get()
				+ retryTime.toMillis() < now){
			return HALF_OPEN;
		}
		return OPEN;
	}

	protected void incrementCounterOnStateChange(String state){
		Counters.inc(String.format("CircuitBreaker %s %s", name, state));
	}

	protected void setClock(Clock clock){
		this.clock = clock;
	}

}
