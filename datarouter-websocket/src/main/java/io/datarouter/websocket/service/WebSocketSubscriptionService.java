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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import io.datarouter.websocket.endpoint.WebSocketService;
import io.datarouter.websocket.storage.session.WebSocketSession;
import io.datarouter.websocket.storage.session.WebSocketSessionKey;
import io.datarouter.websocket.storage.subscription.DatarouterWebSocketSubscriptionDao;
import io.datarouter.websocket.storage.subscription.WebSocketSubscription;
import io.datarouter.websocket.storage.subscription.WebSocketSubscriptionByUserTokenKey;
import io.datarouter.websocket.storage.subscription.WebSocketSubscriptionKey;

@Singleton
public class WebSocketSubscriptionService implements WebSocketService{

	@Inject
	private DatarouterWebSocketSubscriptionDao webSocketSubscriptionDao;
	@Inject
	private Gson gson;

	@Override
	public String getName(){
		return "websocketSubscription";
	}

	@Override
	public void onMessage(WebSocketSession webSocketSession, String message){
		WebSocketSubscriptionRequest request = gson.fromJson(message, WebSocketSubscriptionRequest.class);
		WebSocketSubscriptionKey subscriptionKey = new WebSocketSubscriptionKey(
				request.topic,
				webSocketSession.getKey().getUserToken(),
				webSocketSession.getKey().getId());
		if(request.command == WebSocketSubscriptionCommand.subscribe){
			webSocketSubscriptionDao.put(new WebSocketSubscription(subscriptionKey, null));
		}else if(request.command == WebSocketSubscriptionCommand.unsubscribe){
			webSocketSubscriptionDao.delete(subscriptionKey);
		}
	}

	@Override
	public void onClose(WebSocketSession webSocketSession){
		onSessionVacuum(webSocketSession.getKey());
	}

	@Override
	public void onSessionVacuum(WebSocketSessionKey webSocketSessionKey){
		webSocketSubscriptionDao.scanKeysWithPrefixByUserToken(new WebSocketSubscriptionByUserTokenKey(
				webSocketSessionKey.getUserToken(), webSocketSessionKey.getId(), null))
				.map(WebSocketSubscriptionByUserTokenKey::getTargetKey)
				.flush(webSocketSubscriptionDao::deleteMulti);
	}

	enum WebSocketSubscriptionCommand{
		subscribe,
		unsubscribe,
		;
	}

	public record WebSocketSubscriptionRequest(
			WebSocketSubscriptionCommand command,
			String topic){
	}

}
