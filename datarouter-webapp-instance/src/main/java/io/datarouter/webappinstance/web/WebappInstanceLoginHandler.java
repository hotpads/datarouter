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
package io.datarouter.webappinstance.web;

import javax.inject.Inject;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.webappinstance.service.OneTimeLoginService;

public class WebappInstanceLoginHandler extends BaseHandler{

	public static final String P_USE_IP = "useIp";

	@Inject
	private OneTimeLoginService oneTimeLoginService;

	@Handler
	protected Mav instanceLogin(String webappName, String serverName){
		Session session = getSessionInfo().getRequiredSession();
		Boolean shouldUseIp = params.optionalBoolean(P_USE_IP).orElse(false);
		return oneTimeLoginService.createToken(session, webappName, serverName, shouldUseIp, request);
	}

}
