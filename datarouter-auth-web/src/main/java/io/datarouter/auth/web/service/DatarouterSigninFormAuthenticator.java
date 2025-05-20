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
package io.datarouter.auth.web.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.authenticate.authenticator.DatarouterAuthenticator;
import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.auth.exception.InvalidCredentialsException;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.datarouteruser.cache.DatarouterUserByUsernameCache;
import io.datarouter.auth.storage.user.session.DatarouterSession;
import io.datarouter.types.MilliTime;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.exception.IncorrectPasswordException;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlSettings;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSigninFormAuthenticator implements DatarouterAuthenticator{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterSigninFormAuthenticator.class);

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DatarouterSamlSettings samlSettings;
	@Inject
	private DatarouterUserByUsernameCache datarouterUserByUsernameCache;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private DatarouterAuthPaths paths;

	@Override
	public DatarouterSessionAndPersist getSession(HttpServletRequest request, HttpServletResponse response){
		//the usual case where we're not submitting the login form.  just skip this filter
		if(ObjectTool.notEquals(request.getServletPath(), paths.signin.submit.toSlashedString())){
			return new DatarouterSessionAndPersist(null, false);
		}
		String username = RequestTool.get(request, authenticationConfig.getUsernameParam(), null);
		String password = RequestTool.get(request, authenticationConfig.getPasswordParam(), null);
		if(ObjectTool.anyNull(username, password)){
			return new DatarouterSessionAndPersist(null, false);
		}
		if(samlSettings.getShouldProcess()){
			logger.info("Sign in form disabled.");
			return new DatarouterSessionAndPersist(null, false);
		}

		DatarouterUser user = lookupAndValidateUser(username, password);

		user.setLastLoggedIn(MilliTime.now());
		datarouterUserDao.put(user);

		return new DatarouterSessionAndPersist(DatarouterSession.createFromUser(user), true);
	}


	private DatarouterUser lookupAndValidateUser(String username, String password){
		if(StringTool.isEmpty(username)){
			throw new InvalidCredentialsException("no username specified");
		}

		DatarouterUser user = datarouterUserByUsernameCache.getOrThrow(username);
		if(BooleanTool.isFalseOrNull(user.getEnabled())){
			throw new InvalidCredentialsException("user not enabled (" + username + ")");
		}
		if(StringTool.isEmpty(password)){
			throw new InvalidCredentialsException("password cannot be empty (" + username + ")");
		}
		if(!datarouterUserService.isPasswordCorrect(user, password)){
			throw new IncorrectPasswordException("invalid password (" + username + ")");
		}
		return user;
	}

}
