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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.auth.exception.InvalidCredentialsException;
import io.datarouter.auth.model.dto.InterpretedSamlAssertion;
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.session.Session;
import io.datarouter.auth.session.SessionBasedUser;
import io.datarouter.auth.session.UserSessionService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.session.BaseDatarouterSessionDao;
import io.datarouter.auth.storage.user.session.DatarouterSession;
import io.datarouter.auth.storage.user.session.DatarouterSessionKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.MilliTime;
import io.datarouter.util.BooleanTool;
import io.datarouter.web.user.session.DatarouterSessionManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
	private DatarouterUserService datarouterUserService;
	@Inject
	private RoleManager roleManager;
	@Inject
	private DatarouterUserHistoryService userHistoryService;

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
	public Optional<Session> signInUser(HttpServletRequest request, String username){
		Optional<DatarouterUser> user = datarouterUserService.findUserByUsername(username, true);
		if(user.isEmpty()){
			return Optional.empty();
		}
		if(BooleanTool.isFalseOrNull(user.get().getEnabled())){
			throw new InvalidCredentialsException("user not enabled (" + username + ")");
		}

		user.get().setLastLoggedIn(MilliTime.now());
		userDao.put(user.get());

		DatarouterSession session = DatarouterSession.createFromUser(user.get());
		sessionDao.put(session);
		return Optional.of(session);
	}

	@Override
	public Session signInUserFromSamlResponse(
			HttpServletRequest request,
			InterpretedSamlAssertion interpretedSamlAssertion){
		DatarouterUser user = datarouterUserService.findUserByUsername(interpretedSamlAssertion.username(), true)
				.orElseGet(() -> userCreationService.createAutomaticUser(
						interpretedSamlAssertion.username(),
						DatarouterUserCreationService.SAML_USER_CREATION_DESCRIPTION));
		if(BooleanTool.isFalseOrNull(user.getEnabled())){
			throw new InvalidCredentialsException("user not enabled (" + interpretedSamlAssertion.username() + ")");
		}

		user.setLastLoggedIn(MilliTime.now());
		DatarouterSession session = DatarouterSession.createFromUser(user);

		Optional<SamlChanges> changes = getSamlSignOnChanges(user, interpretedSamlAssertion);
		if(changes.isPresent()){
			userHistoryService.recordSamlSignOnChanges(user, changes.get().changeString());
			user.setSamlGroups(new ArrayList<>(interpretedSamlAssertion.roleGroupAttributes()));
			session.setRoles(changes.get().computedRoles());
		}else{
			session.setRoles(roleManager.calculateRolesWithGroups(user.getRolesIgnoreSaml(), user.getSamlGroups()));
		}
		userDao.put(user);
		sessionDao.put(session);
		return session;
	}

	@Override
	public SessionBasedUser createAuthorizedUser(String username, String description){
		return userCreationService.createAutomaticUser(username, description);
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
	public Optional<ZoneId> getZoneId(String username){
		return userDao.getByUsername(new DatarouterUserByUsernameLookup(username)).getZoneId();
	}

	private record SamlChanges(
			Set<Role> computedRoles,
			String changeString){
	}

	// Ignores roleAttributes
	private Optional<SamlChanges> getSamlSignOnChanges(
			DatarouterUser user,
			InterpretedSamlAssertion interpretedSamlAssertion){
		if(interpretedSamlAssertion.roleGroupAttributes().equals(new HashSet<>(user.getSamlGroups()))){
			return Optional.empty();
		}
		SortedSet<String> lostGroups = Scanner.of(user.getSamlGroups())
				.exclude(interpretedSamlAssertion.roleGroupAttributes()::contains)
				.collect(TreeSet::new);
		SortedSet<String> gainedGroups = Scanner.of(interpretedSamlAssertion.roleGroupAttributes())
				.exclude(user.getSamlGroups()::contains)
				.exclude(Objects::isNull)
				.collect(TreeSet::new);

		Set<Role> previouslyComputedRoles =
				roleManager.calculateRolesWithGroups(user.getRolesIgnoreSaml(), user.getSamlGroups());

		Set<Role> newComputedRoles = roleManager.calculateRolesWithGroups(
				user.getRolesIgnoreSaml(),
				interpretedSamlAssertion.roleGroupAttributes());

		String lostGroupsString = "";
		String lostRolesString = "";
		if(!lostGroups.isEmpty()){
			lostGroupsString = "SAML groups lost: %s.".formatted(String.join(", ", lostGroups));
			SortedSet<Role> lostRoles = Scanner.of(previouslyComputedRoles)
					.exclude(newComputedRoles::contains)
					.collect(TreeSet::new);
			if(lostRoles.isEmpty()){
				lostRolesString = "No roles lost due to lost SAML groups.";
			}else{
				lostRolesString = "Net roles lost: %s.".formatted(
						String.join(", ", lostRoles.stream().map(Role::getPersistentString).toList()));
			}
		}

		String gainedGroupsString = "";
		String gainedRolesString = "";
		if(!gainedGroups.isEmpty()){
			gainedGroupsString = "SAML groups gained: %s.".formatted(String.join(", ", gainedGroups));
			SortedSet<Role> gainedRoles = Scanner.of(newComputedRoles)
					.exclude(previouslyComputedRoles::contains)
					.collect(TreeSet::new);
			if(gainedRoles.isEmpty()){
				gainedRolesString = "No roles provided by new SAML groups.";
			}else{
				gainedRolesString = "Net roles gained: %s.".formatted(
						String.join(", ", gainedRoles.stream().map(Role::getPersistentString).toList()));
			}
		}

		String changeString = String.join("\n", Scanner.of(
				"Changes detected from last SAML sign on.",
				gainedGroupsString,
				lostGroupsString,
				gainedRolesString,
				lostRolesString)
				.exclude(String::isEmpty)
				.list());
		return Optional.of(new SamlChanges(newComputedRoles, changeString));
	}

}
