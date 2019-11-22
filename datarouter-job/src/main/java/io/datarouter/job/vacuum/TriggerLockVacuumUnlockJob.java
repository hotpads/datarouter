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
package io.datarouter.job.vacuum;

import javax.inject.Inject;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.lock.ClusterTriggerLockService;

/**
 * Safety mechanism to make sure the TriggerLockVacuum itself doesn't get stuck.  Register it without a lock.
 */
public class TriggerLockVacuumUnlockJob extends BaseJob{

	@Inject
	private ClusterTriggerLockService clusterTriggerLockService;

	@Override
	public void run(TaskTracker tracker){
		String lockName = BaseTriggerGroup.lockName(ClusterJobLockVacuumJob.class);
		clusterTriggerLockService.deleteJobLockIfExpired(lockName);
	}

}
