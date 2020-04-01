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
package io.datarouter.websocket.job;

import java.util.Collection;

import javax.inject.Inject;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.websocket.WebSocketCounters;
import io.datarouter.websocket.endpoint.WebSocketServices;
import io.datarouter.websocket.session.PushService;
import io.datarouter.websocket.storage.session.DatarouterWebSocketSessionDao;
import io.datarouter.websocket.storage.session.WebSocketSession;
import io.datarouter.websocket.storage.session.WebSocketSessionKey;

public class WebSocketSessionVacuumJob extends BaseJob{

	@Inject
	private DatarouterWebSocketSessionDao dao;
	@Inject
	private PushService pushService;
	@Inject
	private WebSocketServices webSocketServices;

	@Override
	public void run(TaskTracker tracker){
		makeVacuum().run(tracker);
	}

	private DatabeanVacuum<WebSocketSessionKey,WebSocketSession> makeVacuum(){
		return new DatabeanVacuumBuilder<>(
				dao.scan(),
				this::shouldDelete,
				this::delete)
				.build();
	}

	private boolean shouldDelete(WebSocketSession webSocketSession){
		boolean isAlive = pushService.isAlive(webSocketSession);
		if(!isAlive){
			WebSocketCounters.inc("vacuum delete");
		}
		return !isAlive;
	}

	private void delete(Collection<WebSocketSessionKey> keys){
		dao.deleteMulti(keys);
		webSocketServices.listSampleInstances().forEach(service -> keys.forEach(service::onSessionVacuum));
	}

}
