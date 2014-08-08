package com.hotpads.logging;

import javax.inject.Inject;


public class LoggingConfigUpdaterJob implements Runnable{

	@Inject
	private HotPadsLoggingConfigDao hotPadsLoggingConfigDao;
	@Inject
	private HotPadsLog4j2Configurator hotPadsLog4j2Configurator;

	private String previousSignature;

	public void setInitialSignature(String initialSignature){
		this.previousSignature = initialSignature;
	}

	public void setPreviousSignature(String previousSignature){
		this.previousSignature = previousSignature;
	}

	@Override
	public void run(){
		LoggingConfig config = hotPadsLoggingConfigDao.loadConfig();
		if(!config.getSignature().equals(previousSignature)){
			hotPadsLog4j2Configurator.appConfig(config);
			previousSignature = config.getSignature();
		}
	}

}
