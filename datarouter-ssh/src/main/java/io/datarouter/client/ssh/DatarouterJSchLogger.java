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
package io.datarouter.client.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatarouterJSchLogger implements com.jcraft.jsch.Logger{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterJSchLogger.class);

	@Override
	public boolean isEnabled(int level){
		return switch(level){
		case DEBUG -> logger.isDebugEnabled();
		case INFO -> logger.isInfoEnabled();
		case WARN -> logger.isWarnEnabled();
		case ERROR, FATAL -> logger.isErrorEnabled();
		default -> true; // unknown level
		};
	}

	@Override
	public void log(int level, String message){
		switch(level){
		case DEBUG -> logger.debug(message);
		case INFO -> logger.info(message);
		case WARN -> logger.warn(message);
		default -> logger.error(message);
		}
	}

}
