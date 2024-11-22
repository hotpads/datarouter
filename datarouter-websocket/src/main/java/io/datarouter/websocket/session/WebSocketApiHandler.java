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
package io.datarouter.websocket.session;

import java.io.IOException;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.types.Param;
import jakarta.inject.Inject;

public class WebSocketApiHandler extends BaseHandler{

//	private static final String DTO_NAME = new DatarouterHttpClientDefaultConfig().getDtoParameterName();
	private static final String DTO_NAME = "dataTransferObject";

	@Inject
	private WebSocketApiService webSocketApiService;

	/**
	 * same name as WebSocketCommandName.PUSH.getPath()
	 */
	@Handler
	private boolean push(@Param(DTO_NAME) WebSocketCommandDto webSocketCommand) throws IOException{
		return webSocketApiService.push(webSocketCommand);
	}

	/**
	 * same name as WebSocketCommandName.IS_ALIVE.getPath()
	 */
	@Handler
	private boolean isAlive(@Param(DTO_NAME) WebSocketCommandDto webSocketCommand){
		return webSocketApiService.isAlive(webSocketCommand);
	}

}
