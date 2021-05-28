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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletRequest;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleEnum;
import io.datarouter.web.user.session.service.Session;

/**
 * Simple interface for reading information about the current user, session, and roles.
 * Implementations should depend on as few Daos, Services, etc., as possible to minimize risk of circular
 * dependencies. Only {@link CurrentSessionInfo#getSession(ServletRequest)}, and
 * {@link CurrentSessionInfo#getRoles(ServletRequest)} need to be implemented.
 */
public interface CurrentSessionInfo{

	/**
	 * find the username
	 */
	default Optional<String> findNonEmptyUsername(ServletRequest request){
		return getSession(request)
				.map(Session::getUsername)
				.filter(StringTool::notEmpty);
	}

	/**
	 * Gets the username or an alternate value
	 */
	default String getNonEmptyUsernameOrElse(ServletRequest request, String other){
		return findNonEmptyUsername(request).orElse(other);
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

	default boolean hasAnyRole(ServletRequest request, Collection<Role> targetRoles){
		Set<Role> roles = getRoles(request);
		return targetRoles.stream()
				.anyMatch(roles::contains);
	}

	default boolean hasAnyRoleEnum(ServletRequest request, Collection<RoleEnum<?>> targetRoles){
		Set<Role> roles = getRoles(request);
		return targetRoles.stream()
				.map(RoleEnum::getRole)
				.anyMatch(roles::contains);
	}

	class NoOpCurrentSessionInfo implements CurrentSessionInfo{

		@Override
		public Optional<? extends Session> getSession(ServletRequest request){
			return Optional.empty();
		}

		@Override
		public Set<Role> getRoles(ServletRequest request){
			return Collections.emptySet();
		}

	}

}
