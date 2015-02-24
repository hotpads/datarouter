package com.hotpads.util.core.logging;

import java.util.List;

import org.apache.logging.log4j.Level;

public interface LoggingConfigDao{

	void createAndputLoggerConfig(String name, Level level, boolean additive, List<String> appendersRef);

	void deleteLoggerConfig(String name);

	void createAndPutConsoleAppender(String name, String pattern, String targetStr);

	void deleteConsoleAppender(String name);

	void createAndputFileAppender(String name, String pattern, String fileName);

	void deleteFileAppender(String name);

}
