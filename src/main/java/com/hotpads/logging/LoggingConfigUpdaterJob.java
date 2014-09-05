package com.hotpads.logging;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.WebAppName;
import com.hotpads.setting.DatarouterSettings;

@Singleton
public class LoggingConfigUpdaterJob implements Runnable{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private HotPadsLoggingConfigDao hotPadsLoggingConfigDao;
	@Inject
	private HotPadsLog4j2Configurator hotPadsLog4j2Configurator;
	@Inject
	private DatarouterSettings datarouterSettings;
	@Inject
	private WebAppName webAppName;
	
	private String previousSignature;

	public void setInitialSignature(String initialSignature){
		this.previousSignature = initialSignature;
	}

	public void setPreviousSignature(String previousSignature){
		this.previousSignature = previousSignature;
	}

	@Override
	public void run(){
		if(!datarouterSettings.getLoggingConfigUpdaterEnabled().getValue()){
			return;
		}
		LoggingConfig config = hotPadsLoggingConfigDao.loadConfig();
		logger.debug("Logging config updater is running on " + webAppName);
		logger.debug("Logging config signature = " + config.getSignature());
		if(!config.getSignature().equals(previousSignature)){
			logger.info("Logging config apllied on " + webAppName);
			hotPadsLog4j2Configurator.applyConfig(config);
			previousSignature = config.getSignature();
		}
	}

}
