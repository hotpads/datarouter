package com.hotpads.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.hotpads.logging.ClassA.Class1;
import com.hotpads.logging.ClassA.Class1.Class11;
import com.hotpads.logging.ClassA.Class2;
import com.hotpads.logging.another.Class4;
import com.hotpads.logging.apackage.Class3;
import com.hotpads.util.core.logging.Log4j2Configurator;

/*
 * The config for these tests to pass is in TestDatarouterLog4j2Configuration
 */
public class LoggingTests{
	private static final Logger logger = LoggerFactory.getLogger(LoggingTests.class);

	@AfterClass
	public void after(){
		new File(TestDatarouterLog4j2Configuration.TEST_FILE_NAME).delete();
	}

	@Test
	public void staticTest() throws IOException {
		new Class1().logYourName();
		new Class11().logYourName();
		new Class2().logYourName();
		new Class3().logYourName();
		new Class4().logYourName();

		logger.warn("mException", new Exception(new Exception(new NullPointerException())));

		int numberOfLines = getNumberOfLineInLogFile();
		AssertJUnit.assertEquals(5, numberOfLines);
	}

	@Test
	public void dynamiqueTest(){
		Log4j2Configurator log4j2Configurator = new Log4j2Configurator();
		logOneOfEachLevel();
		log4j2Configurator.getRootLoggerConfig().setLevel(Level.DEBUG);
		logOneOfEachLevel();

		log4j2Configurator.updateOrCreateLoggerConfig(Class11.class, Level.INFO, false, new String[]{
				TestDatarouterLog4j2Configuration.CONSOLE_ERR_REF});
		new Class11().logYourName();
	}

	private int getNumberOfLineInLogFile() throws IOException{
		BufferedReader bufferedReader = new BufferedReader(new FileReader(
				TestDatarouterLog4j2Configuration.TEST_FILE_NAME));
		int lineCount = 0;
		while(bufferedReader.readLine() != null){
			lineCount++;
		}
		bufferedReader.close();
		return lineCount;
	}

	private void logOneOfEachLevel(){
		logger.trace("trace");
		logger.debug("debug");
		logger.info("info");
		logger.warn("warn");
		logger.error("error");
	}

}
