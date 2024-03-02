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
package io.datarouter.web.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.authenticate.authenticator.DatarouterAuthenticator;
import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.auth.session.DatarouterSessionManager;
import io.datarouter.auth.storage.user.session.BaseDatarouterSessionDao;
import io.datarouter.auth.storage.user.session.DatarouterSession;
import io.datarouter.auth.storage.user.session.DatarouterSessionKey;
import io.datarouter.types.MilliTime;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

// TODO braydonh: figure out how to move this out of dr-web
@Singleton
public class DatarouterSessionAuthenticator implements DatarouterAuthenticator{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterSessionAuthenticator.class);

	@Inject
	private BaseDatarouterSessionDao datarouterSessionDao;
	@Inject
	private DatarouterSessionManager sessionManager;
	@Inject
	private DatarouterAuthenticationConfig datarouterAuthenticationConfig;

	@Override
	public DatarouterSession getSession(HttpServletRequest request, HttpServletResponse response){
		String sessionToken = sessionManager.getSessionTokenFromCookie(request);
		if(StringTool.isEmptyOrWhitespace(sessionToken)){
			return null;
		}
		DatarouterSession session = datarouterSessionDao.get(new DatarouterSessionKey(sessionToken));
		if(session == null || datarouterAuthenticationConfig.isSessionExpired(session)){
			return null;
		}

		//verify session's userToken matches cookie userToken.  if not, delete session to be safe
		String cookieUserToken = sessionManager.getUserTokenFromCookie(request);
		if(ObjectTool.notEquals(cookieUserToken, session.getUserToken())){
			logger.warn("session userToken " + session.getUserToken() + " != cookie userToken " + cookieUserToken
					+ ", deleting session");
			datarouterSessionDao.delete(session.getKey());
			sessionManager.clearSessionTokenCookie(response);
			sessionManager.clearUserTokenCookie(response);
			return null;
		}

		session.setUpdated(MilliTime.now().toDate());
		return session;
	}

}
