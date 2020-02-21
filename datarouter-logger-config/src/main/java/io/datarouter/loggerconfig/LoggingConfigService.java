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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.loggerconfig.storage.consoleappender.DatarouterConsoleAppenderDao;
import io.datarouter.loggerconfig.storage.fileappender.DatarouterFileAppenderDao;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao;

@Singleton
public class LoggingConfigService{

	@Inject
	private DatarouterLoggerConfigDao loggerConfigDao;
	@Inject
	private DatarouterFileAppenderDao fileAppenderDao;
	@Inject
	private DatarouterConsoleAppenderDao consoleAppenderDao;

	private String previousLoggingConfigSignatureFromUpdaterJob;

	public LoggingConfig loadConfig(){
		return new LoggingConfig(
				consoleAppenderDao.scan().list(),
				fileAppenderDao.scan().list(),
				loggerConfigDao.scan().list());
	}

	public String getPreviousLoggingConfigSignatureForUpdaterJob(){
		return previousLoggingConfigSignatureFromUpdaterJob;
	}

	public void setPreviousLoggingConfigSignatureForUpdaterJob(String loggingConfigSignature){
		this.previousLoggingConfigSignatureFromUpdaterJob = loggingConfigSignature;
	}

}
