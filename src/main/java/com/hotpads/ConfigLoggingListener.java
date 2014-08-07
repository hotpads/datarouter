package com.hotpads;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.logging.ConsoleAppender;
import com.hotpads.logging.FileAppender;
import com.hotpads.logging.LoggerConfig;
import com.hotpads.logging.LoggingConfigNodes;
import com.hotpads.util.core.logging.Log4j2Configurator;

@Singleton
public class ConfigLoggingListener extends HotPadsWebAppListener{

	@Inject
	private LoggingConfigNodes loggingConfigNodes;
	@Inject
	private Log4j2Configurator log4j2Configurator;
	@Inject
	private LoggingConfigUpdaterJob loggingConfigUpdaterJob;

	@Override
	public void onStartUp(){
		loadConfigAndApply();
		startJob();
	}

	private void loadConfigAndApply(){
		Iterable<ConsoleAppender> consoleAppenders = loggingConfigNodes.getConsoleAppender().scan(null, null);// SHould I move this to loggingConfigDao
		for(ConsoleAppender consoleAppender : consoleAppenders){
			log4j2Configurator.addConsoleAppender(consoleAppender.getName(), consoleAppender.getTarget(),
					consoleAppender.getLayout());
		}
		Iterable<FileAppender> fileAppenders = loggingConfigNodes.getFileAppender().scan(null, null);
		for(FileAppender fileAppender : fileAppenders){
			log4j2Configurator.addFileAppender(fileAppender.getName(), fileAppender.getFileName(), fileAppender.getLayout());
		}
		Iterable<LoggerConfig> loggerConfigs = loggingConfigNodes.getLoggerConfig().scan(null, null);
		for(LoggerConfig loggerConfig : loggerConfigs){
			log4j2Configurator.updateOrCreateLoggerConfig(loggerConfig.getName(), loggerConfig.getLevel().getLevel(),
					loggerConfig.getAdditive(), loggerConfig.getAppendersRef());
		}
	}

	private void startJob(){
		loggingConfigUpdaterJob.setWebAppName(servletContext.getServletContextName());
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.submit(new Runnable(){
			
			@Override
			public void run(){
				Thread.currentThread().setName("Logging config updater");
			}
		});
		scheduledExecutor.scheduleAtFixedRate(loggingConfigUpdaterJob, 15, 15, TimeUnit.SECONDS);
	}

	@Override
	public void onShutDown(){}

}
