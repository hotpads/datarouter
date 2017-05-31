package com.hotpads.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

import com.hotpads.datarouter.client.imp.mysql.ddl.execute.DatabaseCreator;
import com.hotpads.datarouter.client.imp.mysql.ddl.execute.SingleTableSchemaUpdateFactory;
import com.hotpads.util.core.logging.HotPadsLog4j2Configuration;
import com.hotpads.util.core.logging.Log4j2Configurator;

public class DatarouterJdbcLog4j2Configuration extends HotPadsLog4j2Configuration{

	public DatarouterJdbcLog4j2Configuration(){
		Appender schemaUpdateAppender = Log4j2Configurator.createConsoleAppender("SchemaUpdate", Target.SYSTEM_OUT,
				"%msg%n");

		registerParent(DatarouterLog4j2Configuration.class);
		addAppender(schemaUpdateAppender);
		addLoggerConfig(SingleTableSchemaUpdateFactory.class.getName(), Level.INFO, false, schemaUpdateAppender);
		addLoggerConfig(DatabaseCreator.class.getName(), Level.INFO, false, schemaUpdateAppender);
	}
}
