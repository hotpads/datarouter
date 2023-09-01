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
package io.datarouter.auth.session;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletRequest;

import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleEnum;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RequestAwareCurrentSessionInfoFactory{

	@Inject
	private CurrentSessionInfo currentSessionInfo;

	public RequestAwareCurrentSessionInfo build(ServletRequest request){
		return new RequestAwareCurrentSessionInfo(currentSessionInfo, request);
	}

	/**
	 * This is a wrapper of {@link CurrentSessionInfo} that does not require passing the request to every method.
	 */
	public static class RequestAwareCurrentSessionInfo{

		private final CurrentSessionInfo currentSessionInfo;
		private final ServletRequest request;

		public RequestAwareCurrentSessionInfo(CurrentSessionInfo currentSessionInfo, ServletRequest request){
			this.currentSessionInfo = currentSessionInfo;
			this.request = request;
		}

		public Optional<String> findNonEmptyUsername(){
			return currentSessionInfo.findNonEmptyUsername(request);
		}

		public String getNonEmptyUsernameOrElse(String other){
			return currentSessionInfo.getNonEmptyUsernameOrElse(request, other);
		}

		public Optional<? extends Session> getSession(){
			return currentSessionInfo.getSession(request);
		}

		public Session getRequiredSession(){
			return currentSessionInfo.getRequiredSession(request);
		}

		public Set<Role> getRoles(){
			return currentSessionInfo.getRoles(request);
		}

		public boolean hasRole(Role role){
			return currentSessionInfo.hasRole(request, role);
		}

		public boolean hasRole(RoleEnum<?> role){
			return currentSessionInfo.hasRole(request, role);
		}

		public boolean hasAnyRole(Collection<Role> targetRoles){
			return currentSessionInfo.hasAnyRole(request, targetRoles);
		}

		public boolean hasAnyRoleEnum(Collection<RoleEnum<?>> targetRoles){
			return currentSessionInfo.hasAnyRoleEnum(request, targetRoles);
		}

	}

}
