package com.hotpads.handler.logging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Singleton;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DataRouterDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.JsonMav;
import com.hotpads.logging.Log4j2Configurator;

@Singleton
public class LoggingSettingsHandler extends BaseHandler{
	private static final String
		JSP = "/logging",
		JSP_CONSOLE_APPENDER = "/consoleAppender",
		JSP_FILE_APPENDER = "/fileAppender";
	private static final Level[] levels = new Level[]{
		Level.ALL,
		Level.TRACE,
		Level.DEBUG,
		Level.INFO,
		Level.WARN,
		Level.ERROR,
		Level.FATAL,//TODO remove because not in slf4j ?
		Level.OFF,
	};

	@Inject
	private Log4j2Configurator log4j2Configurator;

	private Mav redirectMav;

	public Mav getRedirectMav(){
		if(redirectMav == null) {
			redirectMav = new Mav(Mav.REDIRECT + servletContext.getContextPath() + DataRouterDispatcher.URL_DATAROUTER + DataRouterDispatcher.LOGGING);
		}
		return redirectMav;
	}

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav(JSP);
		mav.put("rootLogger", log4j2Configurator.getRootLoggerConfig());
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
	private Mav testLog(){
		String loggerName = params.required("loggerName");
		Logger logger = LoggerFactory.getLogger(loggerName);
		String message = "LoggingSettingsHandler.testLog()";
		logger.trace(message);
		logger.debug(message);
		logger.info(message);
		logger.warn(message);
		logger.error(message);
		return new JsonMav();
	}

	@Handler
	private Mav updateLoggerConfig(){
		updateOrCreateLoggerConfig();
		return getRedirectMav();
	}

	@Handler
	private Mav createLoggerConfig(){
		updateOrCreateLoggerConfig();
		return getRedirectMav();
	}

	private void updateOrCreateLoggerConfig(){
		String[] appenders = request.getParameterValues("appenders");
		String name = params.required("name");
		Level level = Level.getLevel(params.required("level"));
		boolean additive = Boolean.parseBoolean(params.required("additive"));
		log4j2Configurator.updateOrCreateLoggerConfig(name, level, additive, appenders);
	}

	@Handler
	private Mav deleteLoggerConfig(){
		String name = params.required("name");
		log4j2Configurator.deleteLoggerConfig(name);
		return getRedirectMav();
	}

	@Handler
	private Mav deleteAppender(){
		String name = params.required("name");
		log4j2Configurator.deleteAppender(name);
		return getRedirectMav();
	}
	
	@Handler
	private Mav editConsoleAppender(){
		String action = params.optional("action", null);
		String name = params.optional("name", null);
		if(action != null && action.equals("Create")){
			String pattern = params.required("layout");
			String targetStr = params.required("target");
			PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();
			ConsoleAppender appender = ConsoleAppender.createAppender(layout, null, targetStr, name, null, null);
			log4j2Configurator.addAppender(appender);
			return getRedirectMav();
		}
		Mav mav = new Mav(JSP_CONSOLE_APPENDER);
		mav.put("name", name);
//		if(name != null) {
//			ConsoleAppender appender = (ConsoleAppender)log4j2Configurator.getAppender(name);
//			Layout<? extends Serializable> layout = appender.getLayout();
//			mav.put("layout", layout);
//		}
		return mav;
	}
	
	@Handler
	private Mav editFileAppender(){
		String action = params.optional("action", null);
		String name = params.optional("name", null);
		if(action != null && action.equals("Create")){
			String pattern = params.required("layout");
			String fileName = params.required("fileName");
			PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();
			FileAppender appender = FileAppender.createAppender(fileName, null, null, name, null, null, null, null, layout, null, null, null, null);
			log4j2Configurator.addAppender(appender);
			return getRedirectMav();
		}
		Mav mav = new Mav(JSP_FILE_APPENDER);
		mav.put("name", name);
		return mav;
	}
}
