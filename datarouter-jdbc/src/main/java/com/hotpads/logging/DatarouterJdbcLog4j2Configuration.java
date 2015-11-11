package com.hotpads.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;

import com.hotpads.util.core.logging.HotPadsLog4j2Configuration;
import com.hotpads.util.core.logging.Log4j2Configurator;

public class DatarouterJdbcLog4j2Configuration extends HotPadsLog4j2Configuration{

	public DatarouterJdbcLog4j2Configuration(){
		Appender schemaUpdateAppender = Log4j2Configurator.createConsoleAppender("SchemaUpdate", ConsoleAppender.Target
				.SYSTEM_OUT.name(), "%msg%n");

		addAppender(schemaUpdateAppender);
		addLoggerConfig("com.hotpads.datarouter.client.imp.jdbc.ddl.execute.SingleTableSchemaUpdate", Level.INFO, false,
				schemaUpdateAppender);
	}
}
