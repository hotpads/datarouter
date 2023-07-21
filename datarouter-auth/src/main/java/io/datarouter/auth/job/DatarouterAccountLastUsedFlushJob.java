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
package io.datarouter.auth.job;

import io.datarouter.auth.service.DatarouterAccountLastUsedDateService;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import jakarta.inject.Inject;

public class DatarouterAccountLastUsedFlushJob extends BaseJob{

	private final DatarouterAccountLastUsedDateService service;

	@Inject
	public DatarouterAccountLastUsedFlushJob(DatarouterAccountLastUsedDateService service){
		this.service = service;
	}

	@Override
	public void run(TaskTracker tracker){
		service.flush();
	}

}
