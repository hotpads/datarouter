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

import java.util.HashMap;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.websocket.WebSocketCounters;
import io.datarouter.websocket.storage.session.DatarouterWebSocketSessionDao;
import jakarta.inject.Inject;

public class WebSocketSessionDatabaseMonitoringJob extends BaseJob{

	@Inject
	private DatarouterWebSocketSessionDao dao;

	@Override
	public void run(TaskTracker tracker){
		var counter = new HashMap<String,Integer>();
		dao.scan().forEach(webSocket -> {
			tracker.increment();
			counter.merge(webSocket.getServerName(), 1, Integer::sum);
		});
		counter.forEach((server, count) -> WebSocketCounters.saveCount("database " + server, count));
		int total = counter.values().stream().mapToInt(Integer::intValue).sum();
		WebSocketCounters.saveCount("databaseTotal", total);
	}

}
