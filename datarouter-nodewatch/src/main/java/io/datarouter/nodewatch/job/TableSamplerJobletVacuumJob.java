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
package io.datarouter.nodewatch.job;

import java.time.Duration;
import java.time.Instant;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet;
import jakarta.inject.Inject;

public class TableSamplerJobletVacuumJob extends BaseJob{

	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;

	@Override
	public void run(TaskTracker tracker){
		Duration ttl = Duration.ofDays(7);
		Instant threshold = Instant.now().minus(ttl);
		var prefix = JobletRequestKey.create(TableSpanSamplerJoblet.JOBLET_TYPE, null, null, null);
		jobletRequestDao.scanKeysWithPrefix(prefix)
				.include(key -> key.getCreatedInstant().isBefore(threshold))
				.batch(100)
				.forEach(jobletRequestDao::deleteMulti);
	}

}
