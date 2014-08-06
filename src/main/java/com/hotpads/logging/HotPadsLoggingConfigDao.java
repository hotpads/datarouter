package com.hotpads.logging;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Level;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.util.core.logging.LoggingConfigDao;

@Singleton
public class HotPadsLoggingConfigDao implements LoggingConfigDao{

	@Inject
	private SortedMapStorage<LoggerConfigKey, LoggerConfig> loggerConfigNode;
	
	@Override
	public void createAndputLoggerConfig(String name, Level level, boolean additive, Iterable<String> appendersRef){
		LoggerConfig loggerConfig = new LoggerConfig(name, level, additive, appendersRef);
		loggerConfigNode.put(loggerConfig, null);
	}

	@Override
	public void deleteLoggerConfig(String name){
		loggerConfigNode.delete(new LoggerConfigKey(name), null);
	}
}
