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
package io.datarouter.job.job;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.config.properties.ServerName;
import jakarta.inject.Inject;

public class BootTestJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(BootTestJob.class);

	@Inject
	private ServerName serverName;

	@Override
	public void run(TaskTracker tracker) throws Exception{
		logger.warn("Running BootTestJob on {} at {}", serverName.get(), Instant.now());
		Thread.sleep(2000);
		logger.warn("Finished BootTestJob on {} at {}", serverName.get(), Instant.now());
	}

}
