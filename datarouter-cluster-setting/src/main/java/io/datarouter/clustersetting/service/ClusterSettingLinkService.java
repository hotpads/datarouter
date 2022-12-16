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
package io.datarouter.clustersetting.service;

import static j2html.TagCreator.a;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import j2html.tags.specialized.ATag;

@Singleton
public class ClusterSettingLinkService{

	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterClusterSettingPaths paths;

	public ATag makeSettingLink(String settingName){
		String href = emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.settings)
				.withParam("submitAction", "browseSettings")
				.withParam("name", settingName)
				.build();
		return a(settingName)
				.withHref(href)
				.withStyle("text-decoration:none;");
	}

}
