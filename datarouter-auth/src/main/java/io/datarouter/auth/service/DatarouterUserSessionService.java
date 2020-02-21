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
package io.datarouter.auth.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.auth.cache.DatarouterUserByUsernameCache;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.collection.SetTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.web.exception.InvalidCredentialsException;
import io.datarouter.web.user.BaseDatarouterSessionDao;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionKey;
import io.datarouter.web.user.session.DatarouterSessionManager;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.user.session.service.SessionBasedUser;
import io.datarouter.web.user.session.service.UserSessionService;

@Singleton
public class DatarouterUserSessionService implements UserSessionService{

	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private BaseDatarouterSessionDao sessionDao;
	@Inject
	private DatarouterSessionManager sessionManager;
	@Inject
	private DatarouterUserCreationService userCreationService;
	@Inject
	private DatarouterUserByUsernameCache cache;

	@Override
	public void setSessionCookies(HttpServletResponse response, Session session){
		sessionManager.addUserTokenCookie(response, session.getUserToken());
		sessionManager.addSessionTokenCookie(response, session.getSessionToken());
	}

	@Override
	public void clearSessionCookies(HttpServletResponse response){
		sessionManager.clearUserTokenCookie(response);
		sessionManager.clearSessionTokenCookie(response);
	}

	@Override
	public Optional<Session> signInUserWithRoles(HttpServletRequest request, String username, Set<Role> roles){
		DatarouterUser user = cache.get(username).orElse(null);
		if(user == null){
			return Optional.empty();
		}
		if(BooleanTool.isFalseOrNull(user.getEnabled())){
			throw new InvalidCredentialsException("user not enabled (" + username + ")");
		}

		user.setLastLoggedIn(new Date());
		user.setRoles(SetTool.union(roles, user.getRoles()));
		userDao.put(user);

		DatarouterSession session = DatarouterSession.createFromUser(user);
		sessionDao.put(session);
		return Optional.of(session);
	}

	@Override
	public SessionBasedUser createAuthorizedUser(String username, String description, Set<Role> roles){
		return userCreationService.createAutomaticUser(username, description, roles);
	}

	@Override
	public void deleteSession(HttpServletRequest request){
		Optional.ofNullable(sessionManager.getSessionTokenFromCookie(request))
				.map(DatarouterSessionKey::new)
				.ifPresent(sessionDao::delete);
	}

	@Override
	public void deleteUserSessions(List<String> usernames){
		Set<String> usernameSet = new HashSet<>(usernames);
		sessionDao.scan()
				.include(session -> usernameSet.contains(session.getUsername()))
				.map(DatarouterSession::getKey)
				.flush(sessionDao::deleteMulti);
	}

	@Override
	public void deprovisionUsers(List<String> usernames, boolean shouldDisable, boolean shouldDelete){
		deleteUserSessions(usernames);
		List<DatarouterUser> users = userDao.getMultiByUsername(IterableTool.map(usernames,
				DatarouterUserByUsernameLookup::new));
		if(shouldDelete){
			userDao.deleteMulti(IterableTool.map(users, DatarouterUser::getKey));
			return;//return to avoid putting deleted users below
		}
		users.forEach(user -> {
			user.setRoles(List.of());
			if(shouldDisable){
				user.setEnabled(false);
			}
		});
		userDao.putMulti(users);
	}

}
