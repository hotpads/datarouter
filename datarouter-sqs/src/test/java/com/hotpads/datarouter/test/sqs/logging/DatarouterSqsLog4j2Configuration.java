package com.hotpads.datarouter.test.sqs.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

import com.hotpads.datarouter.test.sqs.SqsPerformanceTester;

import io.datarouter.logging.BaseLog4j2Configuration;
import io.datarouter.logging.DatarouterLog4j2Configuration;
import io.datarouter.logging.Log4j2Configurator;

public class DatarouterSqsLog4j2Configuration extends BaseLog4j2Configuration{

	public DatarouterSqsLog4j2Configuration(){
		registerParent(DatarouterLog4j2Configuration.class);
		Appender onlyPrint = Log4j2Configurator.createConsoleAppender("SqsConsole", Target.SYSTEM_OUT, "%msg");
		addLoggerConfig(SqsPerformanceTester.class.getName(), Level.INFO, false, onlyPrint);
	}

}
