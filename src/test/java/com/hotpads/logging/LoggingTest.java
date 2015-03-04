package com.hotpads.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.hotpads.datarouter.test.TestDatarouterInjectorProvider;
import com.hotpads.logging.ClassA.Class1;
import com.hotpads.logging.ClassA.Class1.Class11;
import com.hotpads.logging.ClassA.Class2;
import com.hotpads.logging.another.Class4;
import com.hotpads.logging.apackage.Class3;
import com.hotpads.util.core.logging.Log4j2Configurator;
import com.hotpads.util.core.logging.UtilLog4j2Configuration;

public class LoggingTest{
	private static final Logger logger = LoggerFactory.getLogger(LoggingTest.class);

	@After
	public void after(){
		File f = new File(UtilLog4j2Configuration.testFileName);
		f.delete();
	}

	/*
	 * The config that make this test succeed is in TestDatarouterLog4j2Configuration
	 */
	@Test
	public void staticTest() throws IOException {
		new Class1().logYourName();
		new Class11().logYourName();
		new Class2().logYourName();
		new Class3().logYourName();
		new Class4().logYourName();

		logger.warn("mException", new Exception(new Exception(new NullPointerException())));

		int i = getNumberOfLineInLogFile();
		Assert.assertEquals(5, i);
	}

	@Test
	public void dynamiqueTest(){
		Injector injector = new TestDatarouterInjectorProvider().get();
		Log4j2Configurator log4j2Configurator = injector.getInstance(Log4j2Configurator.class);
		logOneOfEachLevel();
		log4j2Configurator.getRootLoggerConfig().setLevel(Level.DEBUG);
		logOneOfEachLevel();

		log4j2Configurator.updateOrCreateLoggerConfig(Class11.class, Level.INFO, false, new String[]{"Console err"});
		new Class11().logYourName();
	}

	private int getNumberOfLineInLogFile() throws IOException{
		BufferedReader bufferedReader = new BufferedReader(new FileReader(UtilLog4j2Configuration.testFileName));
		int i = 0;
		while(bufferedReader.readLine() != null){
			i++;
		}
		bufferedReader.close();
		return i;
	}

	private void logOneOfEachLevel(){
		logger.trace("trace");
		logger.debug("debug");
		logger.info("info");
		logger.warn("warn");
		logger.error("error");
	}

}
