package com.hotpads.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClassA{
	protected static Logger mLogger;
	public void logYourName() {
		mLogger.trace(getClass() + ".logYourName()");
		mLogger.debug(getClass() + ".logYourName()");
		mLogger.info(getClass() + ".logYourName()");
		mLogger.warn(getClass() + ".logYourName()");
		mLogger.error(getClass() + ".logYourName()");
	}

	public static class Class1 extends ClassA{
		{
			mLogger = LoggerFactory.getLogger(Class1.class);
		}
		
		public static class Class11 extends ClassA{
			{
				mLogger = LoggerFactory.getLogger(Class11.class);
			}
		}
		
	}

	public static class Class2 extends ClassA{
		{
			mLogger = LoggerFactory.getLogger(Class2.class);
		}
	}
}