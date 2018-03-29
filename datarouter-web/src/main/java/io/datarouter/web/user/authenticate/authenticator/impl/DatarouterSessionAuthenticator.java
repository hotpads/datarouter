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
package io.datarouter.web.user.authenticate.authenticator.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.DateTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.user.DatarouterUserNodes;
import io.datarouter.web.user.authenticate.authenticator.DatarouterAuthenticator;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionKey;
import io.datarouter.web.user.session.DatarouterSessionManager;

@Singleton
public class DatarouterSessionAuthenticator implements DatarouterAuthenticator{
	private static Logger logger = LoggerFactory.getLogger(DatarouterSessionAuthenticator.class);

	public static final Long SESSION_TIMOUT_MS = 30L * DateTool.MILLISECONDS_IN_MINUTE;

	@Inject
	private DatarouterUserNodes userNodes;
	@Inject
	private DatarouterSessionManager sessionManager;

	@Override
	public DatarouterSession getSession(HttpServletRequest request, HttpServletResponse response){
		String sessionToken = sessionManager.getSessionTokenFromCookie(request);
		if(StringTool.isEmptyOrWhitespace(sessionToken)){
			return null;
		}
		DatarouterSession session = userNodes.getSessionNode().get(new DatarouterSessionKey(sessionToken), null);
		if(session == null){
			return null;
		}
		long msSinceLastAccess = System.currentTimeMillis() - session.getUpdated().getTime();
		if(msSinceLastAccess > SESSION_TIMOUT_MS){
			return null;
		}

		//verify session's userToken matches cookie userToken.  if not, delete session to be safe
		String cookieUserToken = sessionManager.getUserTokenFromCookie(request);
		if(ObjectTool.notEquals(cookieUserToken, session.getUserToken())){
			logger.warn("session userToken " + session.getUserToken() + " != cookie userToken " + cookieUserToken
					+ ", deleting session");
			userNodes.getSessionNode().delete(session.getKey(), null);
			sessionManager.clearSessionTokenCookie(response);
			sessionManager.clearUserTokenCookie(response);
			return null;
		}

		session.setUpdated(new Date());
		return session;
	}

}
