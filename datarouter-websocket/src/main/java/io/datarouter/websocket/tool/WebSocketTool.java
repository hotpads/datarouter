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
package io.datarouter.websocket.tool;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.util.string.StringTool;

public class WebSocketTool{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketTool.class);

	public static boolean isHandShakeRequest(HttpServletRequest req){
		String upgrade = req.getHeader(HttpHeaders.UPGRADE);
		logger.debug("upgrade header value: {}", upgrade);
		String connection = req.getHeader(HttpHeaders.CONNECTION);
		logger.debug("connection header value: {}", connection);
		String secWebSocketVersion = req.getHeader(HttpHeaders.SEC_WEBSOCKET_VERSION);
		logger.debug("sec_websocket_version header value: {}", secWebSocketVersion);
		return StringTool.notEmpty(secWebSocketVersion);
	}

}
