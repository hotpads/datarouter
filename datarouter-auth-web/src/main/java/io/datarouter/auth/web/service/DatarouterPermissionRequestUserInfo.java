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

import static j2html.TagCreator.a;
import static j2html.TagCreator.text;

import java.util.List;
import java.util.Optional;

import io.datarouter.auth.detail.DatarouterUserExternalDetailService;
import io.datarouter.auth.detail.DatarouterUserProfileLink;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.web.web.DatarouterPermissionRequestHandler;
import j2html.tags.DomContent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterPermissionRequestUserInfo implements PermissionRequestUserInfo{

	@Inject
	private DatarouterUserExternalDetailService userExternalDetailService;

	@Override
	public List<DomContent> getUserInformation(DatarouterUser user){
		Optional<DatarouterUserProfileLink> profile = userExternalDetailService.getUserProfileLink(user.getUsername());
		String userProfileUrl = profile.map(DatarouterUserProfileLink::url).orElse(null);
		String userProfileDescription = profile.map(DatarouterUserProfileLink::name).orElse("user profile");
		var userTr = DatarouterPermissionRequestHandler.createLabelValueTr(
				"User",
				text(user.getUsername() + " - "),
				userProfileUrl == null ? null : a("view " + userProfileDescription).withHref(userProfileUrl));
		return List.of(userTr);
	}

}
