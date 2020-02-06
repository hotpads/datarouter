/**
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
package io.datarouter.loggerconfig;

import javax.inject.Singleton;

import org.apache.logging.log4j.core.Appender;

import io.datarouter.logging.Log4j2Configurator;
import io.datarouter.logging.StartupConfigurationFactory;

@Singleton
public class GenericLog4j2Configurator extends Log4j2Configurator{

	public void applyConfig(LoggingConfig config){
		removeAllAppender();
		config.getConsoleAppenders().forEach(appender -> addConsoleAppender(appender.getKey().getName(),
				appender.getTarget(), appender.getLayout()));
		config.getFileAppenders().forEach(appender -> addFileAppender(appender.getName(), appender.getFileName(),
				appender.getLayout()));
		removeAllConfigLogger();
		config.getLoggerConfigs().forEach(loggerConfig -> updateOrCreateLoggerConfig(loggerConfig.getName(),
				loggerConfig.getLevel().getLevel(), loggerConfig.getAdditive(), loggerConfig.getAppendersRef()));
	}

	private void removeAllConfigLogger(){
		for(org.apache.logging.log4j.core.config.LoggerConfig config : getConfigs().values()){
			if(!StartupConfigurationFactory.staticLoggerConfigs.contains(config)){
				deleteLoggerConfig(config.getName());
			}
		}
	}

	private void removeAllAppender(){
		for(Appender appender : getAppenders().values()){
			if(!StartupConfigurationFactory.staticAppenders.contains(appender)){
				deleteAppender(appender.getName());
			}
		}
	}

}
