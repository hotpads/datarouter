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
package io.datarouter.joblet.execute;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.service.JobletFactory;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.mutable.MutableBoolean;

@Singleton
public class JobletCallableFactory{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private JobletService jobletService;
	@Inject
	private JobletFactory jobletFactory;
	@Inject
	private DatarouterJobletCounters datarouterJobletCounters;

	public JobletCallable create(MutableBoolean shutdownRequested, JobletProcessor jobletProcessor,
			JobletType<?> jobletType, long id){
		return new JobletCallable(datarouterProperties, jobletService, jobletFactory, datarouterJobletCounters,
				shutdownRequested, jobletProcessor, jobletType, id);
	}

}
