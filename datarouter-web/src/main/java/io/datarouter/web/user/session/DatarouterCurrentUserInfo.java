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

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class DatarouterCurrentUserInfo implements CurrentUserInfo{

	@Override
	public String getEmail(HttpServletRequest request){
		DatarouterSession session = DatarouterSessionManager.getFromRequest(request).get();
		return session.getUsername();
	}

	@Override
	public Optional<String> getUserToken(HttpServletRequest request){
		return DatarouterSessionManager.getFromRequest(request)
				.map(DatarouterSession::getUserToken);
	}

}
