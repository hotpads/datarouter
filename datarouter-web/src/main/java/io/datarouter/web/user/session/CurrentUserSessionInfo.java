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
package io.datarouter.web.user.session;

import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletRequest;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleEnum;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.user.session.service.SessionBasedUser;

/**
 * Simple interface for reading information about the current user, session, and roles.
 * Implementations should depend on as few Routers, Daos, Services, etc., as possible to minimize risk of circular
 * dependencies. Only {@link CurrentUserSessionInfo#getUser(ServletRequest)},
 * {@link CurrentUserSessionInfo#getSession(ServletRequest)}, and
 * {@link CurrentUserSessionInfo#getRoles(ServletRequest)} need to be implemented.
 */
public interface CurrentUserSessionInfo{

	Optional<? extends SessionBasedUser> getUser(ServletRequest request);

	default SessionBasedUser getRequiredUser(ServletRequest request){
		return getUser(request).get();
	}

	/**
	 * Gets the username of the current user, most commonly the email address
	 */
	default Optional<String> getUsername(ServletRequest request){
		return getSession(request).map(Session::getUsername);
	}

	/**
	 * Gets the username or an alternate value
	 */
	default String getNonEmptyUsernameOrElse(ServletRequest request, String other){
		return getUsername(request)
				.filter(StringTool::notEmpty)
				.orElse(other);
	}

	default Optional<String> getToken(ServletRequest request){
		return getSession(request).map(Session::getUserToken);
	}

	default Optional<Long> getId(ServletRequest request){
		return getSession(request).map(Session::getUserId);
	}

	Optional<? extends Session> getSession(ServletRequest request);

	default Session getRequiredSession(ServletRequest request){
		return getSession(request).get();
	}

	/**
	 * returns the Roles associated with the current user/session, an empty set if there is no current session
	 */
	Set<Role> getRoles(ServletRequest request);

	/**
	 * returns whether the current user/session is associated with the given Role, false if there is no current session
	 */
	default boolean hasRole(ServletRequest request, Role role){
		return getRoles(request).contains(role);
	}

	/**
	 * returns whether the current user/session is associated with the given Role, false if there is no current session
	 */
	default boolean hasRole(ServletRequest request, RoleEnum<?> role){
		return hasRole(request, role.getRole());
	}

}
