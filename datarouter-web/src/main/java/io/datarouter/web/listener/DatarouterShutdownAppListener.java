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
package io.datarouter.web.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.Datarouter;
import io.datarouter.web.monitoring.BuildProperties;
import io.datarouter.web.monitoring.GitProperties;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterShutdownAppListener implements DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterShutdownAppListener.class);

	@Inject
	private Datarouter datarouter;
	@Inject
	private GitProperties gitProperties;
	@Inject
	private BuildProperties buildProperties;

	@Override
	public void onStartUp(){
		// change to info level when we are printing all info logs
		logger.warn("starting application commit={} build={}", gitProperties.getDescribeShort()
				.orElse(GitProperties.UNKNOWN_STRING), buildProperties.getBuildId());
	}

	@Override
	public void onShutDown(){
		logger.info("datarouter.shutdown()");
		datarouter.shutdown();
	}

	@Override
	public boolean safeToExecuteInParallel(){
		return false;
	}

}
