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
package io.datarouter.job.scheduler;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.web.listener.DatarouterAppListener;

@Singleton
public class JobSchedulerAppListener implements DatarouterAppListener{

	@Inject
	private JobScheduler jobScheduler;
	@Inject
	private TriggerGroupClasses triggerGroupClasses;

	@Override
	public final void onStartUp(){
		triggerGroupClasses.get().forEach(jobScheduler::registerTriggers);
	}

	@Override
	public final void onShutDown(){
		jobScheduler.shutdown();
	}

}
