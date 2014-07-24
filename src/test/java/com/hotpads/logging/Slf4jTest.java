package com.hotpads.logging;

import org.apache.logging.log4j.Level;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Slf4jTest{
	private static final Logger logger = LoggerFactory.getLogger(Slf4jTest.class);

	@Test
	public void test() {
		Injector injector = Guice.createInjector();
		Log4j2Configurator log4j2Configurator = injector.getInstance(Log4j2Configurator.class);
		logOneOfEachLevel();
		log4j2Configurator.setRootLevel(Level.DEBUG);
		logOneOfEachLevel();
		
		log4j2Configurator.setLevel(Class2.class, Level.INFO);
		new Class1().logYourName();
		new Class2().logYourName();
	}

	private void logOneOfEachLevel(){
		logger.trace("trace");
		logger.debug("debug");
		logger.info("info");
		logger.warn("warn");
		logger.error("error");
	}
	
	public static abstract class ClassA{
		static Logger mLogger;
		public void logYourName() {
			mLogger.trace(getClass() + ".logYourName()");
			mLogger.debug(getClass() + ".logYourName()");
			mLogger.info(getClass() + ".logYourName()");
			mLogger.warn(getClass() + ".logYourName()");
			mLogger.error(getClass() + ".logYourName()");
		}
	}
	
	public static class Class1 extends ClassA{
		{
			mLogger = LoggerFactory.getLogger(Class1.class);
		}
	}

	public static class Class2 extends ClassA{
		{
			mLogger = LoggerFactory.getLogger(Class2.class);
		}
	}
}
