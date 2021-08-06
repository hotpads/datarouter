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
package io.datarouter.websocket.endpoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.MessageHandler;

import io.datarouter.websocket.session.PushService;

public class SwedishEchoMessageHandler implements ClosableMessageHandler, MessageHandler.Whole<String>{

	@Singleton
	public static class SwedishEchoMessageHandlerFactory{

		@Inject
		private PushService pushService;

		public SwedishEchoMessageHandler create(String userToken){
			return new SwedishEchoMessageHandler(pushService, userToken);
		}

	}

	private final PushService pushService;
	private final String userToken;

	public SwedishEchoMessageHandler(PushService pushService, String userToken){
		this.pushService = pushService;
		this.userToken = userToken;
	}

	@Override
	public void onMessage(String message){
		pushService.forwardToAll(userToken, message + " " + message);
	}

}
