package com.hotpads.handler.exception;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRunnable implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(LoggingRunnable.class);

	@Singleton
	public static class LoggingRunnableFactory{

		@Inject
		private ExceptionRecorder exceptionRecorder;

		public LoggingRunnable create(Runnable runnable){
			return new LoggingRunnable(exceptionRecorder, runnable);
		}

	}

	private final Runnable runnable;
	private final ExceptionRecorder exceptionRecorder;

	private LoggingRunnable(ExceptionRecorder exceptionRecorder, Runnable runnable){
		this.exceptionRecorder = exceptionRecorder;
		this.runnable = runnable;
	}

	@Override
	public void run(){
		try{
			runnable.run();
		}catch(Throwable t){
			logger.warn("Exception while running {}", runnable, t);
			exceptionRecorder.tryRecordException(t, runnable.getClass().getName());
		}
	}

}
