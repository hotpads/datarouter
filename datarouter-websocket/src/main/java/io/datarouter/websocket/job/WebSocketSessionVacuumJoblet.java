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
package io.datarouter.websocket.job;

import java.time.Instant;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.joblet.codec.DatarouterBaseGsonJobletCodec;
import io.datarouter.joblet.model.BaseJoblet;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletType.JobletTypeBuilder;
import io.datarouter.storage.tag.Tag;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;
import io.datarouter.tasktracker.service.LongRunningTaskTrackerFactory;
import jakarta.inject.Inject;

public class WebSocketSessionVacuumJoblet extends BaseJoblet<Instant>{

	public static final JobletType<Instant> JOBLET_TYPE = new JobletTypeBuilder<>(
			"WebSocketSessionVacuum",
			WebSocketSessionVacuumJobletCodec::new,
			WebSocketSessionVacuumJoblet.class)
			.withTag(Tag.DATAROUTER)
			.build();

	@Inject
	private WebSocketSessionVacuum webSocketSessionVacuum;
	@Inject
	private LongRunningTaskTrackerFactory longRunningTaskTrackerFactory;

	@Override
	public void process(){
		TaskTracker tracker = longRunningTaskTrackerFactory.create(
				WebSocketSessionVacuumJoblet.class.getSimpleName(),
				LongRunningTaskType.JOBLET,
				Instant.MAX,
				true,
				"");
		tracker.start();
		webSocketSessionVacuum.run(tracker);
		tracker.finish();
	}

	public static class WebSocketSessionVacuumJobletCodec
	extends DatarouterBaseGsonJobletCodec<Instant>{

		public WebSocketSessionVacuumJobletCodec(){
			super(Instant.class);
		}

		@Override
		public int calculateNumItems(Instant params){
			return 1;
		}

	}

}
