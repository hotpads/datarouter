package com.hotpads.example.config.guice;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.example.config.ExampleSettingFinder;
import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionNodes;
import com.hotpads.handler.exception.NoOpExceptionHandlingConfig;
import com.hotpads.handler.exception.NoOpExceptionNodes;
import com.hotpads.profile.metrics.MetricsNodes;
import com.hotpads.profile.metrics.NoOpMetricsNodes;
import com.hotpads.util.core.logging.LoggingConfigDao;
import com.hotpads.util.core.logging.NonPersistentLoggingConfigDao;

public class ExampleGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(SettingFinder.class).to(ExampleSettingFinder.class);
		bind(ExceptionHandlingConfig.class).to(NoOpExceptionHandlingConfig.class);
		bind(ExceptionNodes.class).to(NoOpExceptionNodes.class);
		bind(LoggingConfigDao.class).to(NonPersistentLoggingConfigDao.class);
		bind(MetricsNodes.class).to(NoOpMetricsNodes.class);
	}

}
