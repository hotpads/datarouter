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
package io.datarouter.joblet.execute;

import java.util.concurrent.atomic.AtomicLong;

import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.queue.JobletRequestQueueManager;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.type.JobletType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobletProcessorFactory{

	@Inject
	private DatarouterJobletSettingRoot jobletSettings;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private JobletCallableFactory jobletCallableFactory;
	@Inject
	private DatarouterJobletCounters datarouterJobletCounters;
	@Inject
	private JobletService jobletService;

	public JobletProcessor create(AtomicLong idGenerator, JobletType<?> jobletType){
		return new JobletProcessor(jobletSettings, jobletRequestQueueManager, jobletCallableFactory,
				datarouterJobletCounters, jobletService, idGenerator, jobletType);
	}

}