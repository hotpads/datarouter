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
import org.apache.logging.log4j.core.config.LoggerConfig;

import io.datarouter.logging.Log4j2Configurator;
import io.datarouter.logging.StartupConfigurationFactory;
import io.datarouter.scanner.Scanner;

@Singleton
public class GenericLog4j2Configurator extends Log4j2Configurator{

	public void applyConfig(LoggingConfig config){
		removeAllAppender();
		config.getConsoleAppenders().forEach(appender -> addConsoleAppender(
				appender.getKey().getName(),
				appender.getTarget(),
				appender.getLayout()));
		config.getFileAppenders().forEach(appender -> addFileAppender(
				appender.getName(),
				appender.getFileName(),
				appender.getLayout()));
		removeAllConfigLogger();
		config.getLoggerConfigs().forEach(loggerConfig -> updateOrCreateLoggerConfig(
				loggerConfig.getName(),
				loggerConfig.getLevel().getLevel(),
				loggerConfig.getAdditive(),
				loggerConfig.getAppendersRef()));
	}

	private void removeAllConfigLogger(){
		Scanner.of(getConfigs().values())
				.exclude(StartupConfigurationFactory.staticLoggerConfigs::contains)
				.map(LoggerConfig::getName)
				.forEach(this::deleteLoggerConfig);
	}

	private void removeAllAppender(){
		Scanner.of(getAppenders().values())
				.exclude(StartupConfigurationFactory.staticAppenders::contains)
				.map(Appender::getName)
				.forEach(this::deleteAppender);
	}

}
