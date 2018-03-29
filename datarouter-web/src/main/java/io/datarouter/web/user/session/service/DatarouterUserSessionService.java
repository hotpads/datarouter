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
package io.datarouter.web.user.session.service;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.util.BooleanTool;
import io.datarouter.util.collection.SetTool;
import io.datarouter.web.exception.InvalidCredentialsException;
import io.datarouter.web.user.DatarouterUserCreationService;
import io.datarouter.web.user.DatarouterUserDao;
import io.datarouter.web.user.DatarouterUserNodes;
import io.datarouter.web.user.cache.DatarouterUserByUsernameCache;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionManager;

@Singleton
public class DatarouterUserSessionService implements UserSessionService{

	@Inject
	private DatarouterUserNodes userNodes;
	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private DatarouterSessionManager datarouterSessionManager;
	@Inject
	private DatarouterUserCreationService datarouterUserCreationService;
	@Inject
	private DatarouterUserByUsernameCache datarouterUserByUsernameCache;

	@Override
	public void setSessionCookies(HttpServletResponse response, Session session){
		datarouterSessionManager.addUserTokenCookie(response, session.getUserToken());
		datarouterSessionManager.addSessionTokenCookie(response, session.getSessionToken());
	}

	@Override
	public void clearSessionCookies(HttpServletResponse response){
		datarouterSessionManager.clearUserTokenCookie(response);
		datarouterSessionManager.clearSessionTokenCookie(response);
	}

	@Override
	public SessionBasedUser getValidatedCurrentUser(HttpServletRequest request){
		return DatarouterSessionManager.getFromRequest(request)
				.map(userDao::getAndValidateCurrentUser)
				.get();
	}

	@Override
	public Optional<SessionBasedUser> findUser(String username){
		return Optional.ofNullable(datarouterUserByUsernameCache.get(username).orElse(null));
	}

	@Override
	public Optional<Session> signInUserWithRoles(HttpServletRequest request, String username, Set<Role> roles){
		DatarouterUser user = datarouterUserByUsernameCache.get(username).orElse(null);
		if(user == null){
			return Optional.empty();
		}
		if(BooleanTool.isFalseOrNull(user.getEnabled())){
			throw new InvalidCredentialsException("user not enabled (" + username + ")");
		}

		user.setLastLoggedIn(new Date());
		user.setRoles(SetTool.union(getDatarouterUserRoles(roles),user.getRoles()));
		userNodes.getUserNode().put(user, null);

		DatarouterSession session = DatarouterSession.createFromUser(user);
		userNodes.getSessionNode().put(session, null);
		return Optional.of(session);
	}

	@Override
	public SessionBasedUser createAuthorizedUser(HttpServletRequest request, String username, String description,
			Set<Role> roles){
		return datarouterUserCreationService.createAutomaticUser(username, description, getDatarouterUserRoles(roles));
	}

	private static Set<DatarouterUserRole> getDatarouterUserRoles(Set<Role> roles){
		return roles.stream()
				.map(Role::getPersistentString)
				.map(DatarouterUserRole::fromPersistentStringStatic)
				.collect(Collectors.toSet());
	}

}
