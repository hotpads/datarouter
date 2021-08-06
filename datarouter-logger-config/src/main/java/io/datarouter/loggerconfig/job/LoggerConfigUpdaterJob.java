/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.loggerconfig.job;

import java.util.Collection;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.loggerconfig.GenericLog4j2Configurator;
import io.datarouter.loggerconfig.LoggingConfig;
import io.datarouter.loggerconfig.LoggingConfigService;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao;
import io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig;

public class LoggerConfigUpdaterJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(LoggerConfigUpdaterJob.class);

	@Inject
	private LoggingConfigService loggingConfigService;
	@Inject
	private DatarouterLoggerConfigDao loggerConfigDao;
	@Inject
	private GenericLog4j2Configurator configurator;

	@Override
	public void run(TaskTracker tracker){
		LoggingConfig config = loggingConfigService.loadConfig();
		expireLoggerConfigs(config);
		String previousSignature = loggingConfigService.getPreviousLoggingConfigSignatureForUpdaterJob();
		logger.debug("Logging config signature={} and previousSignature={}", config.getSignature(), previousSignature);

		if(!config.getSignature().equals(previousSignature)){
			logger.debug("Logging config applied with signature={}", config.getSignature());
			configurator.applyConfig(config);
			loggingConfigService.setPreviousLoggingConfigSignatureForUpdaterJob(config.getSignature());
			tracker.setLastItemProcessed(config.getSignature());
		}
	}

	private void expireLoggerConfigs(LoggingConfig config){
		Collection<LoggerConfig> expiredLoggerConfigs = loggerConfigDao.expireLoggerConfigs(config);
		if(!expiredLoggerConfigs.isEmpty()){
			logger.debug("Expired LoggerConfigs={}", expiredLoggerConfigs);
		}
	}

}
