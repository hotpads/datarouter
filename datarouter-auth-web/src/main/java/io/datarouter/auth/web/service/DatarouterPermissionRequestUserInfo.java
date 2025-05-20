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

import java.util.List;
import java.util.Optional;

import io.datarouter.auth.detail.DatarouterUserExternalDetailService;
import io.datarouter.auth.detail.DatarouterUserProfileLink;
import io.datarouter.auth.service.PermissionRequestUserInfo;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterPermissionRequestUserInfo implements PermissionRequestUserInfo{

	@Inject
	private DatarouterUserExternalDetailService userExternalDetailService;

	@Override
	public List<UserInfo> getUserInformation(DatarouterUser user){
		String username = user.getUsername();
		Optional<DatarouterUserProfileLink> profile = userExternalDetailService.getUserProfileLink(username);

		return List.of(new UserInfo(
				"User",
				profile.map(DatarouterUserProfileLink::name).orElse(username),
				profile.map(DatarouterUserProfileLink::url),
				Optional.empty()));
	}

}
