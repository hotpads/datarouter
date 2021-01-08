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
package io.datarouter.conveyor;

import java.io.InterruptedIOException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.exception.ConveyorExceptionCategory;
import io.datarouter.util.concurrent.UncheckedInterruptedException;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.util.ExceptionTool;

public abstract class BaseConveyor implements Conveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseConveyor.class);

	protected final String name;
	protected final ExceptionRecorder exceptionRecorder;
	private final Supplier<Boolean> shouldRunSetting;
	private final Supplier<Boolean> compactExceptionLogging;

	public BaseConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			Supplier<Boolean> compactExceptionLogging,
			ExceptionRecorder exceptionRecorder){
		this.name = name;
		this.shouldRunSetting = shouldRun;
		this.compactExceptionLogging = compactExceptionLogging;
		this.exceptionRecorder = exceptionRecorder;
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
				if(!result.shouldContinueImmediately){
					break;
				}
			}
			long duration = System.currentTimeMillis() - start;
			ConveyorCounters.incFinishDrain(this);
			logger.info("drain finished for conveyor={} duration={} iterations={} ", name, duration, iteration);
		}catch(Throwable e){
			boolean isInterrupted = ExceptionTool.isFromInstanceOf(e, InterruptedException.class,
					UncheckedInterruptedException.class, InterruptedIOException.class);
			if(isInterrupted){
				try{
					interrupted();
				}catch(Exception ex){
					logger.error(ex.getMessage(), ex);
				}
			}
			ConveyorCounters.incException(this);
			if(getCompactExceptionLogging()){
				logger.warn("swallowing exception so ScheduledExecutorService restarts this Runnable {}", e.toString());
			}else{
				logger.warn("swallowing exception so ScheduledExecutorService restarts this Runnable", e);
			}
			if(!isInterrupted){
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

	private boolean getCompactExceptionLogging(){
		try{
			return compactExceptionLogging.get();
		}catch(Exception e){
			logger.warn("invalid value for compactExceptionLogging", e);
			return false;
		}
	}

}
