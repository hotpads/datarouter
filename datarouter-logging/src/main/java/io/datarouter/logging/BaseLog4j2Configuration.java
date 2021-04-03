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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.LoggerConfig;

public abstract class BaseLog4j2Configuration{

	public static final String DEFAULT_PATTERN = "%d %-5level [%t]"
			+ " <%equals{${web:servletContextName}}{${web:servletContextName}}{%pid}>" // servlet name or pid
			+ " %logger{36}:%line - %msg%n%rEx";

	private static final String DEFAULT_CATALINA_OUT_DIR = "/mnt/logs";
	private static final String CATALINA_OUT_DIR_ENV_VARIABLE = "CATALINA_OUT_DIR";
	public static final String CATALINA_OUT_DIR;
	static{
		String catalinaOutDir = System.getenv(CATALINA_OUT_DIR_ENV_VARIABLE);
		if(catalinaOutDir == null || catalinaOutDir.isEmpty()){
			CATALINA_OUT_DIR = DEFAULT_CATALINA_OUT_DIR;
		}else{
			CATALINA_OUT_DIR = catalinaOutDir;
		}
	}

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
		appenders.put(appender.getName(), appender);
	}

	public final Collection<LoggerConfig> getLoggerConfigs(){
		return loggerConfigs.values();
	}

	protected final void addLoggerConfig(String name, Level level, boolean additive, Appender... appenders){
		addLoggerConfig(name, level, additive, List.of(appenders));
	}

	private final void addLoggerConfig(String name, Level level, boolean additive, Iterable<Appender> appenders){
		LoggerConfig loggerConfig = new LoggerConfig(name, level, additive);
		appenders.forEach(appender -> loggerConfig.addAppender(appender, null, null));
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
			configuration = clazz.getDeclaredConstructor().newInstance();
		}catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
			throw new RuntimeException(e);
		}
		configuration.getAppenders().forEach(this::addAppender);
		configuration.getLoggerConfigs().forEach(loggerConfig -> addLoggerConfig(
				loggerConfig.getName(),
				loggerConfig.getLevel(),
				loggerConfig.isAdditive(),
				loggerConfig.getAppenders().values()));
		configuration.getFilters().forEach(this::addFilter);
	}

}
