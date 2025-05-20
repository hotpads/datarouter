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
package io.datarouter.websocket.service;

import io.datarouter.gson.DatarouterGsons;
import io.datarouter.websocket.endpoint.WebSocketService;
import io.datarouter.websocket.session.PushService;
import io.datarouter.websocket.storage.session.WebSocketSession;
import io.datarouter.websocket.storage.session.WebSocketSessionKey;
import jakarta.inject.Inject;

public class WebSocketIdService implements WebSocketService{

	@Inject
	private PushService pushService;

	@Override
	public String getName(){
		return "id";
	}

	@Override
	public void onMessage(WebSocketSession webSocketSession, String message){
		WebSocketIdResponse response = new WebSocketIdResponse(webSocketSession.getKey());
		pushService.forward(webSocketSession, DatarouterGsons.withUnregisteredEnums().toJson(response));
	}

	public static class WebSocketIdResponse{

		public final String type;
		public final String userToken;
		public final String sessionId;

		public WebSocketIdResponse(WebSocketSessionKey key){
			this.type = "id";
			this.userToken = key.getUserToken();
			this.sessionId = key.getId().toString();
		}

	}

}
