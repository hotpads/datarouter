package com.hotpads.logging;

import java.util.List;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.LoggerConfig;

public interface Log4j2Configuration{

	List<Appender> getAppenders();

	List<LoggerConfig> getLoggerConfigs();

}
