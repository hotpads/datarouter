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
package io.datarouter.web.exception;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRunnable implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(LoggingRunnable.class);

	@Singleton
	public static class LoggingRunnableFactory{

		@Inject
		private Optional<ExceptionRecorder> exceptionRecorder;

		public LoggingRunnable create(Runnable runnable){
			return new LoggingRunnable(exceptionRecorder, runnable);
		}

	}

	private final Runnable runnable;
	private final Optional<ExceptionRecorder> exceptionRecorder;

	private LoggingRunnable(Optional<ExceptionRecorder> exceptionRecorder, Runnable runnable){
		this.exceptionRecorder = exceptionRecorder;
		this.runnable = runnable;
	}

	@Override
	public void run(){
		try{
			runnable.run();
		}catch(Throwable t){
			logger.warn("Exception while running {}", runnable, t);
			exceptionRecorder.ifPresent(recorder -> recorder.tryRecordException(t, runnable.getClass().getName()));
		}
	}

}
