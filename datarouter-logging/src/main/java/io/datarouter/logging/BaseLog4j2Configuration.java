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
package io.datarouter.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.LoggerConfig;

public abstract class BaseLog4j2Configuration{

	public static final String defaultPattern = "%d %-5level [%t] <${web:servletContextName:-${sys:user.name}}>"
			+ " %logger{36}:%line - %msg%n%rEx";

	private final Map<String,Appender> appenders = new HashMap<>();
	private final List<Filter> filters = new ArrayList<>();
	private final Map<String,LoggerConfig> loggerConfigs = new HashMap<>();

	public final Collection<Appender> getAppenders(){
		return appenders.values();
	}

	protected final Appender getAppender(String name){
		return appenders.get(name);
	}

	protected final void addAppender(Appender appender){
		if(appenders.containsKey(appender.getName())){
			throw new IllegalArgumentException("Duplicate appender declaration : " + appender.getName());
		}
		appenders.put(appender.getName(), appender);
	}

	public final Collection<LoggerConfig> getLoggerConfigs(){
		return loggerConfigs.values();
	}

	protected final void addLoggerConfig(String name, Level level, boolean additive, Appender... appenders){
		addLoggerConfig(name, level, additive, Arrays.asList(appenders));
	}

	private final void addLoggerConfig(String name, Level level, boolean additive, Iterable<Appender> appenders){
		LoggerConfig loggerConfig = new LoggerConfig(name, level, additive);
		for(Appender appender : appenders){
			loggerConfig.addAppender(appender, null, null);
		}
		if(loggerConfigs.containsKey(loggerConfig.getName())){
			throw new IllegalArgumentException("Duplicate logger config declaration : " + loggerConfig.getName());
		}
		loggerConfigs.put(loggerConfig.getName(), loggerConfig);
	}

	protected final Collection<Filter> getFilters(){
		return filters;
	}

	protected final void addFilter(Filter filter){
		filters.add(filter);
	}

	protected final void registerParent(Class<? extends BaseLog4j2Configuration> clazz){
		BaseLog4j2Configuration configuration;
		try{
			configuration = clazz.newInstance();
		}catch(InstantiationException | IllegalAccessException e){
			throw new RuntimeException(e);
		}
		for(Appender appender : configuration.getAppenders()){
			addAppender(appender);
		}
		for(LoggerConfig loggerConfig : configuration.getLoggerConfigs()){
			addLoggerConfig(loggerConfig.getName(), loggerConfig.getLevel(), loggerConfig.isAdditive(), loggerConfig
					.getAppenders().values());
		}
		for(Filter filter : configuration.getFilters()){
			addFilter(filter);
		}
	}

}
