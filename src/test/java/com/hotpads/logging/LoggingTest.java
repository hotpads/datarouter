package com.hotpads.logging;

import java.util.Map;

import junit.framework.Assert;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.logging.ClassA.Class1;
import com.hotpads.logging.ClassA.Class1.Class11;
import com.hotpads.logging.ClassA.Class2;
import com.hotpads.logging.another.Class3;

public class LoggingTest{
	private static Logger logger;
	{
		System.setProperty("log4j.configurationFactory", "com.hotpads.logging.StartupConfigurationFactory"); 
		logger = LoggerFactory.getLogger(LoggingTest.class);
	}

	@Test
	public void test() {
		Injector injector = Guice.createInjector();
		Log4j2Configurator log4j2Configurator = injector.getInstance(Log4j2Configurator.class);
		logOneOfEachLevel();
		log4j2Configurator.setRootLevel(Level.DEBUG);
		logOneOfEachLevel();

		new Class1().logYourName();
		log4j2Configurator.updateOrCreateLoggerConfig(Class11.class, Level.TRACE, false, new String[]{"Console"});
		new Class11().logYourName();
		log4j2Configurator.updateOrCreateLoggerConfig(Class2.class, Level.INFO, false, new String[]{"Console"});
		new Class2().logYourName();
		new Class3().logYourName();

		Map<String,LoggerConfig> configs = log4j2Configurator.getConfigs();
//		System.out.println(configs);
		Assert.assertEquals(4, configs.size()); //Bad test because depend of the xml file
//		System.out.println(log4j2Configurator.getAppenders());
	}

	private void logOneOfEachLevel(){
		logger.trace("trace");
		logger.debug("debug");
		logger.info("info");
		logger.warn("warn");
		logger.error("error");
	}

}
