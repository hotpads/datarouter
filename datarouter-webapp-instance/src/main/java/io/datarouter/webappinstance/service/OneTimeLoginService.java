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
package io.datarouter.webappinstance.service;

import java.time.Instant;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.util.Require;
import io.datarouter.web.exception.InvalidCredentialsException;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.user.authenticate.DatarouterTokenGenerator;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.webappinstance.config.DatarouterWebappInstancePaths;
import io.datarouter.webappinstance.storage.onetimelogintoken.DatarouterOneTimeLoginTokenDao;
import io.datarouter.webappinstance.storage.onetimelogintoken.OneTimeLoginToken;
import io.datarouter.webappinstance.storage.onetimelogintoken.OneTimeLoginTokenKey;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstanceKey;
import io.datarouter.webappinstance.web.WebappInstanceHandler;

@Singleton
public class OneTimeLoginService{

	@Inject
	private DatarouterOneTimeLoginTokenDao oneTimeLoginDao;
	@Inject
	private DatarouterWebappInstanceDao webappInstanceDao;
	@Inject
	private DatarouterWebappInstancePaths paths;

	public Mav createToken(
			Session session,
			String webappName,
			String serverName,
			Boolean shouldUseIp,
			HttpServletRequest request){
		Require.noNulls(session, session.getUserId());
		WebappInstance target = webappInstanceDao.get(new WebappInstanceKey(webappName, serverName));
		if(target == null){
			return new MessageMav("specified web app instance does not exist: " + serverName);
		}

		String token = DatarouterTokenGenerator.generateRandomToken();
		Instant deadline = Instant.now().plusSeconds(5);
		var oneTimeLoginToken = new OneTimeLoginToken(
				session.getUserId(),
				token,
				serverName,
				target.getServerPublicIp(),
				Date.from(deadline));
		oneTimeLoginDao.put(oneTimeLoginToken);

		var mav = new GlobalRedirectMav(buildRedirectUrl(request, target, shouldUseIp), true);
		mav.put(WebappInstanceHandler.P_USER_ID, session.getUserId());
		mav.put(WebappInstanceHandler.P_TOKEN, token);
		return mav;
	}

	private String buildRedirectUrl(HttpServletRequest request, WebappInstance target, Boolean shouldUseIp){
		return new StringBuilder().append(request.getScheme())
				.append("://")
				.append(shouldUseIp ? target.getServerPublicIp() : target.getKey().getServerName())
				.append(":")
				.append(request.getServerPort())
				.append(target.getServletContextPath())
				.append(paths.datarouter.webappInstances.toSlashedString())
				.toString();
	}

	public void validateToken(Long userIdParam, String tokenParam, String requestServerName){
		OneTimeLoginToken authenticatedToken = getAuthenticatedToken(userIdParam);

		if(!tokenParam.equals(authenticatedToken.getToken())){
			throw new InvalidCredentialsException("invalid one time token for user " + userIdParam);
		}
		boolean requestServerNameValid = requestServerName.equals(authenticatedToken.getTargetServerName())
				|| requestServerName.equals(authenticatedToken.getTargetServerIp());
		if(!requestServerNameValid){
			throw new InvalidCredentialsException("targetServerName mismatch: authenticated targetServerName="
					+ authenticatedToken.getTargetServerName() + ", actual serverName=" + requestServerName
					+ " for user " + userIdParam);
		}
	}

	//get then delete OneTimeLoginToken
	private OneTimeLoginToken getAuthenticatedToken(Long userId){
		var key = new OneTimeLoginTokenKey(userId);
		OneTimeLoginToken authenticatedToken = oneTimeLoginDao.get(key);
		if(authenticatedToken == null){
			throw new InvalidCredentialsException("No authenticated token exists for user " + userId);
		}
		oneTimeLoginDao.delete(key);
		if(new Date().after(authenticatedToken.getDeadline())){
			throw new InvalidCredentialsException("expired one time token for user " + userId);
		}
		return authenticatedToken;
	}

}
