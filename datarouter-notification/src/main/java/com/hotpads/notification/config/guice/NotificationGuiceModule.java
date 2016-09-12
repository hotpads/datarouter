package com.hotpads.notification.config.guice;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionNodes;
import com.hotpads.handler.exception.NoOpExceptionHandlingConfig;
import com.hotpads.handler.exception.NoOpExceptionNodes;
import com.hotpads.notification.config.NotificationDatarouterProperties;
import com.hotpads.notification.config.NotificationSettingFinder;
import com.hotpads.profile.metrics.MetricsNodes;
import com.hotpads.profile.metrics.NoOpMetricsNodes;
import com.hotpads.util.core.logging.LoggingConfigDao;
import com.hotpads.util.core.logging.NonPersistentLoggingConfigDao;

public class NotificationGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(SettingFinder.class).to(NotificationSettingFinder.class);
		bind(DatarouterProperties.class).to(NotificationDatarouterProperties.class);
		bind(ExceptionHandlingConfig.class).to(NoOpExceptionHandlingConfig.class);
		bind(ExceptionNodes.class).to(NoOpExceptionNodes.class);
		bind(LoggingConfigDao.class).to(NonPersistentLoggingConfigDao.class);
		bind(MetricsNodes.class).to(NoOpMetricsNodes.class);
	}

}
