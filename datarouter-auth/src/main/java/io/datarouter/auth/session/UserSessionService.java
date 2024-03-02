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

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.auth.model.dto.InterpretedSamlAssertion;

public interface UserSessionService{

	void setSessionCookies(HttpServletResponse response, Session session);
	void clearSessionCookies(HttpServletResponse response);

	SessionBasedUser createAuthorizedUser(String username, String description);

	Session signInUserFromSamlResponse(HttpServletRequest request, InterpretedSamlAssertion interpretedSamlAssertion);

	//deletes the Session of the current request
	void deleteSession(HttpServletRequest request);

	//looks up all Sessions for each user and deletes them
	void deleteUserSessions(List<String> usernames);

	Optional<ZoneId> getZoneId(String username);

	class NoOpUserSessionService implements UserSessionService{

		@Override
		public void setSessionCookies(HttpServletResponse response, Session session){
		}

		@Override
		public void clearSessionCookies(HttpServletResponse response){
		}

		@Override
		public SessionBasedUser createAuthorizedUser(String username, String description){
			return null;
		}

		@Override
		public Session signInUserFromSamlResponse(
				HttpServletRequest request,
				InterpretedSamlAssertion interpretedSamlAssertion){
			return null;
		}

		@Override
		public void deleteSession(HttpServletRequest request){
		}

		@Override
		public void deleteUserSessions(List<String> usernames){
		}

		@Override
		public Optional<ZoneId> getZoneId(String username){
			return Optional.empty();
		}

	}

}
