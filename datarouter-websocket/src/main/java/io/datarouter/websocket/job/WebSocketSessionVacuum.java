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

import java.net.NoRouteToHostException;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.response.exception.DatarouterHttpConnectionAbortedException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRuntimeException;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.websocket.WebSocketCounters;
import io.datarouter.websocket.endpoint.WebSocketServices;
import io.datarouter.websocket.session.PushService;
import io.datarouter.websocket.storage.session.DatarouterWebSocketSessionDao;
import io.datarouter.websocket.storage.session.WebSocketSession;
import io.datarouter.websocket.storage.session.WebSocketSessionKey;

@Singleton
public class WebSocketSessionVacuum{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionVacuum.class);

	@Inject
	private DatarouterWebSocketSessionDao dao;
	@Inject
	private PushService pushService;
	@Inject
	private WebSocketServices webSocketServices;

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
		boolean isAlive;
		String reason = null;
		try{
			isAlive = pushService.isAlive(webSocketSession);
			reason = "remoteResponse";
		}catch(DatarouterHttpRuntimeException e){
			// assume alive so that we retry the test on next job run
			isAlive = true;
			Throwable throwable = e.getCause();
			if(throwable != null && throwable instanceof DatarouterHttpConnectionAbortedException){
				throwable = throwable.getCause();
				if(throwable != null && throwable instanceof ConnectTimeoutException){
					// the server is offline, has probably been decommissioned
					isAlive = false;
					reason = "connectTimeout";
				}else if(throwable != null && throwable instanceof NoRouteToHostException){
					isAlive = false;
					reason = "noRouteToHost";
				}
			}else if(throwable != null && throwable instanceof DatarouterHttpResponseException){
				DatarouterHttpResponseException httpException = (DatarouterHttpResponseException)throwable;
				// the websocket api is not accessible, the server address got repurposed for something else
				if(httpException.getResponse().getStatusCode() == 404){
					isAlive = false;
					reason = "404";
				}
			}
			if(reason == null){
				logger.warn("error testing websocket webSocketSession={}", webSocketSession, e);
			}
		}
		if(!isAlive){
			WebSocketCounters.inc("vacuum delete");
			WebSocketCounters.inc("vacuum delete " + reason);
		}
		return !isAlive;
	}

	private void delete(Collection<WebSocketSessionKey> keys){
		dao.deleteMulti(keys);
		webSocketServices.listSampleInstances().forEach(service -> keys.forEach(service::onSessionVacuum));
	}

}
