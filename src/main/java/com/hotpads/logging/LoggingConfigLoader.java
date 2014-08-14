package com.hotpads.logging;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.HotPadsWebAppListener;

@Singleton
public class LoggingConfigLoader extends HotPadsWebAppListener{

	@Inject
	private HotPadsLoggingConfigDao hotPadsLoggingConfigDao;
	@Inject
	private HotPadsLog4j2Configurator hotPadsLog4j2Configurator;
	@Inject
	private LoggingConfigUpdaterJob loggingConfigUpdaterJob;

	@Override
	public void onStartUp(){
		String signature = loadConfigAndApply();
		startJob(signature);
	}

	private String loadConfigAndApply(){
		LoggingConfig config = hotPadsLoggingConfigDao.loadConfig();
		hotPadsLog4j2Configurator.applyConfig(config);
		return config.getSignature();
	}

	private void startJob(String initialSignature){
		loggingConfigUpdaterJob.setInitialSignature(initialSignature);
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
