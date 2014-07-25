package com.hotpads.handler.logging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DataRouterDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.logging.Log4j2Configurator;

public class LoggingSettingsHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(LoggingSettingsHandler.class);
	private static final String JSP = "/logging";
	private static final Level[] levels = new Level[]{
		Level.ALL,
		Level.TRACE,
		Level.DEBUG,
		Level.INFO,
		Level.WARN,
		Level.ERROR,
		Level.FATAL,
		Level.OFF,
	};

	@Inject
	private Log4j2Configurator log4j2Configurator;

	@Override
	protected Mav handleDefault() throws Exception{
		
		String message = "lalalala barbara";
		logger.trace(message);
		logger.debug(message);
		logger.info(message);
		logger.warn(message);
		logger.error(message);
		Mav mav = new Mav(JSP);
		mav.put("rootLogger", log4j2Configurator.getRootLoggerConfig());
//		Level rootLevel = log4j2Configurator.getRootLevel();
//		mav.put("rootLevel", rootLevel);
		mav.put("levels", levels);
		mav.put("booleans", new Boolean[]{true, false});
		Map<String, LoggerConfig> configs = log4j2Configurator.getConfigs();
		Map<LoggerConfig, Collection<String>> appenderMap = new HashMap<>();
		for(LoggerConfig config : configs.values()){
			appenderMap.put(config, config.getAppenders().keySet());
		}
		mav.put("appenderMap", appenderMap);
		mav.put("configs", new TreeMap<>(configs));
		mav.put("appenders", log4j2Configurator.getAppenders());
		return mav;
	}
	
	@Handler
	private Mav updateLoggerConfig(){
		updateOrCreateLoggerConfig();
		return new Mav(Mav.REDIRECT + servletContext.getContextPath() + DataRouterDispatcher.URL_DATAROUTER + DataRouterDispatcher.LOGGING);
	}
	
	@Handler
	private Mav createLoggerConfig(){
		updateOrCreateLoggerConfig();
		return new Mav(Mav.REDIRECT + servletContext.getContextPath() + DataRouterDispatcher.URL_DATAROUTER + DataRouterDispatcher.LOGGING);
	}

	private void updateOrCreateLoggerConfig(){
		String name = params.required("name");
		Level level = Level.getLevel(params.required("level"));
		boolean additive = Boolean.parseBoolean(params.required("additive"));
		log4j2Configurator.updateOrCreateLoggerConfig(name, level, additive);
	}
}
