package com.hotpads.logging;

import org.apache.logging.log4j.Level;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.logging.ClassA.Class1;
import com.hotpads.logging.ClassA.Class1.Class11;
import com.hotpads.logging.ClassA.Class2;
import com.hotpads.logging.another.Class3;

public class Slf4jTest{
	private static final Logger logger = LoggerFactory.getLogger(Slf4jTest.class);

	@Test
	public void test() {
		Injector injector = Guice.createInjector();
		Log4j2Configurator log4j2Configurator = injector.getInstance(Log4j2Configurator.class);
		logOneOfEachLevel();
		log4j2Configurator.setRootLevel(Level.DEBUG);
		logOneOfEachLevel();
		
		new Class1().logYourName();
		new Class11().logYourName();
		log4j2Configurator.setLevel(Class2.class, Level.INFO);
		new Class2().logYourName();
		log4j2Configurator.setLevel("com.hotpads.logging.another" , Level.TRACE);
		new Class3().logYourName();
	}

	private void logOneOfEachLevel(){
		logger.trace("trace");
		logger.debug("debug");
		logger.info("info");
		logger.warn("warn");
		logger.error("error");
	}

}
