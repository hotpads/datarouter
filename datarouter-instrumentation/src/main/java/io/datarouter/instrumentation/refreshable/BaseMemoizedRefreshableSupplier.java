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
package io.datarouter.instrumentation.refreshable;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseMemoizedRefreshableSupplier<T> implements RefreshableSupplier<T>{
	private static final Logger logger = LoggerFactory.getLogger(BaseMemoizedRefreshableSupplier.class);

	public static final Duration DEFAULT_ATTEMPT_INTERVAL = Duration.ofSeconds(15L);

	private final Duration minimumTtl;
	private final Duration attemptInterval;

	private Instant refreshInstant;
	private Instant attemptInstant;
	private T memoizedValue;

	public BaseMemoizedRefreshableSupplier(Duration minimumTtl){
		this(minimumTtl, DEFAULT_ATTEMPT_INTERVAL);
	}

	public BaseMemoizedRefreshableSupplier(Duration minimumTtl, Duration attemptInterval){
		if(minimumTtl.isNegative()){
			throw new IllegalArgumentException();
		}
		if(attemptInterval.isNegative()){
			throw new IllegalArgumentException();
		}
		this.minimumTtl = minimumTtl;
		this.attemptInterval = attemptInterval;
		this.refreshInstant = Instant.EPOCH;
		this.attemptInstant = Instant.EPOCH;
	}

	@Override
	public T get(){
		if(memoizedValue == null){
			refresh();
		}
		return memoizedValue;
	}

	@Override
	public Instant refresh(){
		if(!shouldRefresh()){
			return refreshInstant;
		}
		synchronized(this){
			refreshInternal();
			return refreshInstant;
		}
	}

	protected abstract T readNewValue();

	protected abstract String getIdentifier();

	// NOTE: not synchronized
	private boolean shouldRefresh(){
		return refreshInstant.plus(minimumTtl).isBefore(Instant.now())
				&& attemptInstant.plus(attemptInterval).isBefore(Instant.now());
	}

	private synchronized void refreshInternal(){
		if(!shouldRefresh()){
			return;
		}
		try{
			logger.debug("attempting to refresh identifier={} lastRefresh={} lastAttempt={}", getIdentifier(),
					refreshInstant, attemptInstant);
			T newValue = readNewValue();
			if(newValue == null){
				throw new NullPointerException();
			}
			if(!newValue.equals(memoizedValue)){
				memoizedValue = newValue;
				refreshInstant = Instant.now();
			}
		}catch(RuntimeException e){
			if(memoizedValue == null){
				// fail if initial value can't be found
				throw e;
			}
			logger.warn("failed to refresh identifier={}", getIdentifier(), e);
		}finally{
			attemptInstant = Instant.now();
		}
	}

}
