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
package io.datarouter.websocket.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.datarouter.websocket.tool.WebSocketTool;

public abstract class WebSocketAuthenticationFilter implements Filter{

	public static final String WEB_SOCKET_TOKEN = "WebSocketToken";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	throws IOException, ServletException{
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		if(WebSocketTool.isHandShakeRequest(req)){
			String retrieveUserToken = getUserTokenRetriever().retrieveUserToken(req, res);
			HttpSession httpSession = req.getSession();
			httpSession.setAttribute(WEB_SOCKET_TOKEN, retrieveUserToken);
		}
		chain.doFilter(request, response);
	}

	protected abstract UserTokenRetriever getUserTokenRetriever();

}
