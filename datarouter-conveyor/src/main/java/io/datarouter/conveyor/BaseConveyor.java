/*
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
package io.datarouter.conveyor;

import java.io.InterruptedIOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.exception.ConveyorExceptionCategory;
import io.datarouter.util.Require;
import io.datarouter.util.concurrent.UncheckedInterruptedException;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.util.ExceptionTool;

public abstract class BaseConveyor implements ConveyorRunnable{
	private static final Logger logger = LoggerFactory.getLogger(BaseConveyor.class);

	protected final String name;
	protected final ExceptionRecorder exceptionRecorder;
	protected final ConveyorGaugeRecorder gaugeRecorder;
	private final Supplier<Boolean> shouldRunSetting;
	private final Supplier<Boolean> compactExceptionLogging;
	private final AtomicBoolean isShuttingDown;

	public BaseConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			Supplier<Boolean> compactExceptionLogging,
			ExceptionRecorder exceptionRecorder,
			ConveyorGaugeRecorder gaugeRecorder){
		this.name = Require.notBlank(name);
		this.shouldRunSetting = shouldRun;
		this.compactExceptionLogging = compactExceptionLogging;
		this.exceptionRecorder = exceptionRecorder;
		this.gaugeRecorder = gaugeRecorder;
		this.isShuttingDown = new AtomicBoolean();
	}

	public abstract ProcessBatchResult processBatch();

	@SuppressWarnings("unused")
	public void interrupted() throws Exception{
	}

	protected static class ProcessBatchResult{

		public final boolean shouldContinueImmediately;

		public ProcessBatchResult(boolean shouldContinueImmediately){
			this.shouldContinueImmediately = shouldContinueImmediately;
		}

	}

	@Override
	public void run(){
		if(!shouldRun()){
			return;
		}
		try{
			int iteration = 0;
			long start = System.currentTimeMillis();
			while(shouldRun()){
				iteration++;
				ProcessBatchResult result = processBatch();
				ConveyorCounters.incProcessBatch(this);
				if(!result.shouldContinueImmediately){
					break;
				}
			}
			long duration = System.currentTimeMillis() - start;
			ConveyorCounters.incFinishDrain(this);
			logger.info("drain finished for conveyor={} duration={} iterations={} ", name, duration, iteration);
		}catch(Throwable e){
			boolean interrupted = ExceptionTool.isFromInstanceOf(e, InterruptedException.class,
					UncheckedInterruptedException.class, InterruptedIOException.class);
			if(interrupted){
				ConveyorCounters.incInterrupted(this);
				try{
					interrupted();
				}catch(Exception ex){
					logger.error("interuption handling failed", ex);
				}
			}else{
				ConveyorCounters.incException(this);
			}
			if(getCompactExceptionLogging()){
				logger.warn("swallowing exception so ScheduledExecutorService restarts this Runnable interrupted={} {}",
						interrupted, e);
			}else{
				logger.warn("swallowing exception so ScheduledExecutorService restarts this Runnable interrupted={}",
						interrupted, e);
			}
			if(!interrupted){
				exceptionRecorder.tryRecordException(e, getClass().getName(), ConveyorExceptionCategory.CONVEYOR);
			}
		}
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public boolean shouldRun(){
		return !Thread.currentThread().isInterrupted() && shouldRunSetting.get();
	}

	@Override
	public void setIsShuttingDown(){
		isShuttingDown.set(true);
	}

	@Override
	public boolean isShuttingDown(){
		return isShuttingDown.get();
	}

	private boolean getCompactExceptionLogging(){
		try{
			return compactExceptionLogging.get();
		}catch(Exception e){
			logger.warn("invalid value for compactExceptionLogging", e);
			return false;
		}
	}

}
