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
package com.hotpads.util.core.logging;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.LoggerFactory;

@Singleton
public class Log4j2Configurator{
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Log4j2Configurator.class);

	private static PatternLayout createLayout(String pattern){
		return PatternLayout.newBuilder()
				.withAlwaysWriteExceptions(false)
				.withPattern(pattern)
				.build();
	}

	public static ConsoleAppender createConsoleAppender(String name, Target target, String pattern){
		PatternLayout layout = createLayout(pattern);
		return ConsoleAppender.newBuilder()
				.withLayout(layout)
				.setTarget(target)
				.withName(name)
				.build();
	}

	public static FileAppender createFileAppender(String name, String fileName, String pattern){
		PatternLayout layout = createLayout(pattern);
		return FileAppender.newBuilder()
				.withFileName(fileName)
				.withName(name)
				.withLayout(layout)
				.build();
	}

	private final LoggerContext ctx;
	private final Configuration config;
	private final LoggerConfig rootLoggerConfig;

	public Log4j2Configurator(){
		this.ctx = (LoggerContext)LogManager.getContext(false);
		this.config = ctx.getConfiguration();
		Logger root = LogManager.getRootLogger();
		this.rootLoggerConfig = config.getLoggerConfig(root.getName());
	}

	public void updateOrCreateLoggerConfig(Class<?> clazz, Level level, boolean additive, String... appendersRef){
		updateOrCreateLoggerConfig(clazz.getName(), level, additive, appendersRef);
	}

	public void updateOrCreateLoggerConfig(Package pkg, Level level, boolean additive, String... appendersRef){
		updateOrCreateLoggerConfig(pkg.getName(), level, additive, appendersRef);
	}

	public void updateOrCreateLoggerConfig(String name, Level level, boolean additive, String... appendersRef){
		updateOrCreateLoggerConfig(name, level, additive, Arrays.asList(appendersRef));
	}

	public void updateOrCreateLoggerConfig(String name, Level level, boolean additive, Iterable<String> appendersRef){
		LoggerConfig loggerConfig = config.getLoggerConfig(name);
		if(!loggerConfig.getName().equals(name)){ // it is the parent that have been found
			loggerConfig = new LoggerConfig(name, level, additive);
			config.addLogger(name, loggerConfig);
		}else{
			loggerConfig.setLevel(level);
			loggerConfig.setAdditive(additive);
		}
		if(appendersRef != null){
			updateAppenders(loggerConfig, appendersRef);
		}
		ctx.updateLoggers();
	}

	private void updateAppenders(LoggerConfig loggerConfig, Iterable<String> appendersRef){
		Map<String,Appender> appenders = loggerConfig.getAppenders();
		for(String string : appenders.keySet()){
			loggerConfig.removeAppender(string);
		}
		for(String appenderRef : appendersRef){
			Appender appender = config.getAppender(appenderRef);
			if(appender != null){
				loggerConfig.addAppender(appender, null, null);
			}else{
				logger.error("Appender \"" + appenderRef + "\" not found");
			}
		}
	}

	public void deleteLoggerConfig(String name){
		config.removeLogger(name);
	}

	public void deleteAppender(String name){
		((AbstractConfiguration)config).removeAppender(name);
	}

	public Map<String,LoggerConfig> getConfigs(){
		return config.getLoggers();
	}

	public Map<String,Appender> getAppenders(){
		return config.getAppenders();
	}

	public Appender getAppender(String name){
		return config.getAppender(name);
	}

	public LoggerConfig getRootLoggerConfig(){
		return rootLoggerConfig;
	}

	public void addAppender(Appender appender){
		config.addAppender(appender);
	}

	public void addConsoleAppender(String name, Target target, String pattern){
		Appender appender = Log4j2Configurator.createConsoleAppender(name, target, pattern);
		addAppender(appender);
	}

	public void addFileAppender(String name, String fileName, String pattern){
		Appender appender = Log4j2Configurator.createFileAppender(name, fileName, pattern);
		addAppender(appender);
	}

	public void shutdown(){
		ctx.stop();
	}

}