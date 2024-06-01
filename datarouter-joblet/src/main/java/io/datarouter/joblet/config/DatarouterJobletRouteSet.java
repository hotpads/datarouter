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
package io.datarouter.joblet.config;

import io.datarouter.auth.role.DatarouterUserRole;
import io.datarouter.joblet.handler.JobletExceptionHandler;
import io.datarouter.joblet.handler.JobletHandler;
import io.datarouter.joblet.handler.JobletQueuesHandler;
import io.datarouter.joblet.handler.JobletThreadCountHandler;
import io.datarouter.joblet.handler.JobletUpdateHandler;
import io.datarouter.joblet.handler.RunningJobletsHandler;
import io.datarouter.joblet.handler.SleepingJobletHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobletRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterJobletRouteSet(DatarouterJobletPaths paths){
		handle(paths.datarouter.joblets.copyJobletRequestsToQueues).withHandler(JobletUpdateHandler.class);
		handle(paths.datarouter.joblets.createSleepingJoblets).withHandler(SleepingJobletHandler.class);
		handle(paths.datarouter.joblets.deleteGroup).withHandler(JobletUpdateHandler.class);
		handle(paths.datarouter.joblets.exceptions).withHandler(JobletExceptionHandler.class);
		handle(paths.datarouter.joblets.kill).withHandler(RunningJobletsHandler.class);
		handle(paths.datarouter.joblets.list).withHandler(JobletHandler.class);
		handle(paths.datarouter.joblets.queues).withHandler(JobletQueuesHandler.class);
		handle(paths.datarouter.joblets.restart).withHandler(JobletUpdateHandler.class);
		handle(paths.datarouter.joblets.running).withHandler(RunningJobletsHandler.class);
		handle(paths.datarouter.joblets.threadCounts).withHandler(JobletThreadCountHandler.class);
		handle(paths.datarouter.joblets.timeoutStuckRunning).withHandler(JobletUpdateHandler.class);
		handle(paths.datarouter.joblets.deleteFailedJobletsByIds).withHandler(JobletUpdateHandler.class);
		handle(paths.datarouter.joblets.restartFailedJobletsByIds).withHandler(JobletUpdateHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN, DatarouterUserRole.DATAROUTER_JOB)
				.withTag(Tag.DATAROUTER);
	}

}
