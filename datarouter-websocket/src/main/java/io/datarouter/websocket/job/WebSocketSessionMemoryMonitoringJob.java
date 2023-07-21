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

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.websocket.WebSocketCounters;
import io.datarouter.websocket.service.ServerAddressProvider;
import io.datarouter.websocket.service.WebSocketConnectionStore;
import jakarta.inject.Inject;

public class WebSocketSessionMemoryMonitoringJob extends BaseJob{

	@Inject
	private WebSocketConnectionStore webSocketConnectionStore;
	@Inject
	private WebSocketCounters webSocketCounters;
	@Inject
	private ServerAddressProvider serverAddressProvider;

	@Override
	public void run(TaskTracker tracker){
		int connectionCount = webSocketConnectionStore.list().size();
		webSocketCounters.saveCount("memory " + serverAddressProvider.get(), connectionCount);
		tracker.increment(connectionCount);
	}

}
