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
package io.datarouter.auth.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.text;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.web.DatarouterPermissionRequestHandler;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.detail.DatarouterUserExternalDetailService;
import j2html.tags.DomContent;

@Singleton
public class DatarouterPermissionRequestUserInfo implements PermissionRequestUserInfo{

	@Inject
	private DatarouterUserExternalDetailService userExternalDetailService;

	@Override
	public List<DomContent> getUserInformation(DatarouterUser user){
		String userProfileUrl = userExternalDetailService.getUserProfileUrl(user).orElse(null);
		String userProfileDescription = userExternalDetailService.getUserProfileDescription()
				.orElse("user profile");
		var userTr = DatarouterPermissionRequestHandler.createLabelValueTr(
				"User",
				text(user.getUsername() + " - "),
				userProfileUrl == null ? null : a("view " + userProfileDescription).withHref(userProfileUrl));
		return List.of(userTr);
	}

}
