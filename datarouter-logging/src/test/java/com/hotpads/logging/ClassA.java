package com.hotpads.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClassA{

	protected Logger logger;

	public void logYourName() {
		doAllLog(logger, getClass() + ".logYourName()");
	}

	public static void doAllLog(Logger logger, String message){
		logger.trace(message);
		logger.debug(message);
		logger.info(message);
		logger.warn(message);
		logger.error(message);
	}

	public static class Class1 extends ClassA{
		{
			logger = LoggerFactory.getLogger(Class1.class);
		}

		public static class Class11 extends ClassA{
			{
				logger = LoggerFactory.getLogger(Class11.class);
			}
		}

	}

	public static class Class2 extends ClassA{
		{
			logger = LoggerFactory.getLogger(Class2.class);
		}
	}

}