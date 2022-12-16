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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.exception.ConveyorExceptionCategory;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.util.ExceptionTool;

@Singleton
public class ConveyorService{
	private static final Logger logger = LoggerFactory.getLogger(ConveyorService.class);

	@Inject
	private ExceptionRecorder exceptionRecorder;

	public void run(ConveyorConfiguration configuration, ConveyorRunnable conveyor){
		if(!shouldRun(conveyor)){
			return;
		}

		try{
			int iteration = 0;
			long start = System.currentTimeMillis();
			while(shouldRun(conveyor)){
				iteration++;
				ProcessResult result = configuration.process(conveyor);
				ConveyorCounters.incProcessBatch(conveyor);
				if(!result.shouldContinueImmediately()){
					break;
				}
			}
			long duration = System.currentTimeMillis() - start;
			ConveyorCounters.incFinishDrain(conveyor);
			logger.info("drain finished for conveyor={} duration={} iterations={} ", conveyor.getName(), duration,
					iteration);
		}catch(Throwable e){
			boolean interrupted = ExceptionTool.isInterrupted(e);
			if(interrupted){
				ConveyorCounters.incInterrupted(conveyor);
				try{
					configuration.interrupted(conveyor);
				}catch(Exception ex){
					logger.error("interuption handling failed", ex);
				}
			}else{
				ConveyorCounters.incException(conveyor);
			}
			if(configuration.compactExceptionLogging().get()){
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

	private static boolean shouldRun(ConveyorRunnable conveyor){
		return !Thread.currentThread().isInterrupted() && conveyor.shouldRun();
	}

}
