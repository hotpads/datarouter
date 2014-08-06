package com.hotpads;

import javax.inject.Inject;
import javax.inject.Named;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.logging.LoggerConfig;
import com.hotpads.logging.LoggerConfigKey;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;
import com.hotpads.util.core.logging.Log4j2Configurator;

public class ConfigLoggingListener implements HotPadsWebAppListener{

	@Inject
	@Named(value = "loggerConfigNode")
	private SortedMapStorage<LoggerConfigKey, LoggerConfig> loggerConfigNode;
	@Inject
	private Log4j2Configurator log4j2Configurator;

	@Override
	public void onStartUp(){
		SortedScannerIterable<LoggerConfig> loggerConfigs = loggerConfigNode.scan(null, null);
		for(LoggerConfig loggerConfig : loggerConfigs){
			log4j2Configurator.updateOrCreateLoggerConfig(loggerConfig.getName(), loggerConfig.getLevel().getLevel(),
					loggerConfig.getAdditive(), loggerConfig.getAppendersRef());
		}
	}

	@Override
	public void onShutDown(){}

}
