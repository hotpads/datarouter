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

import java.util.List;

import io.datarouter.loggerconfig.storage.consoleappender.ConsoleAppender;
import io.datarouter.loggerconfig.storage.fileappender.FileAppender;
import io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig;

public class LoggingConfig{

	public static String computeSignature(List<ConsoleAppender> consoles, List<FileAppender> files,
			List<LoggerConfig> loggers){
		StringBuilder builder = new StringBuilder();
		for(ConsoleAppender consoleAppender : consoles){
			builder.append(consoleAppender.getKey().getName());
			builder.append(consoleAppender.getLayout());
			builder.append(consoleAppender.getTarget());
		}
		for(FileAppender fileAppender : files){
			builder.append(fileAppender.getName());
			builder.append(fileAppender.getLayout());
			builder.append(fileAppender.getFileName());
		}
		for(LoggerConfig loggerConfig : loggers){
			builder.append(loggerConfig.getName());
			builder.append(loggerConfig.getLevel());
			builder.append(loggerConfig.getAdditive());
			builder.append(loggerConfig.getAppendersRef());
		}
		return builder.toString();
	}

	private List<ConsoleAppender> consoleAppenders;
	private List<FileAppender> fileAppenders;
	private List<LoggerConfig> loggerConfigs;

	public LoggingConfig(List<ConsoleAppender> consoleAppenders, List<FileAppender> fileAppenders,
			List<LoggerConfig> loggerConfigs){
		this.consoleAppenders = consoleAppenders;
		this.fileAppenders = fileAppenders;
		this.loggerConfigs = loggerConfigs;
	}

	public List<ConsoleAppender> getConsoleAppenders(){
		return consoleAppenders;
	}

	public List<FileAppender> getFileAppenders(){
		return fileAppenders;
	}

	public List<LoggerConfig> getLoggerConfigs(){
		return loggerConfigs;
	}

	public String getSignature(){
		return computeSignature(consoleAppenders, fileAppenders, loggerConfigs);
	}

}
