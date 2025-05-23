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
package io.datarouter.clustersetting.web.tag;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;

import java.util.Optional;

import io.datarouter.clustersetting.link.ClusterSettingTagsLink;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.storage.setting.cached.CachedClusterSettingTags;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class ClusterSettingTagsHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CachedClusterSettingTags cachedClusterSettingTags;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;
	@Inject
	private ClusterSettingTagsHtml html;
	@Inject
	private DatarouterLinkClient linkClient;

	@Handler
	public Mav tags(ClusterSettingTagsLink tags){

		Optional<String> tagName = tags.tagName;
		Optional<Boolean> tagEnabled = tags.tagEnabled;
		// toggle tag and redirect
		if(tagName.isPresent()){
			cachedClusterSettingTags.updateTag(tagName.orElseThrow(), tagEnabled.orElse(false));
			return new GlobalRedirectMav(linkClient.toInternalUrl(new ClusterSettingTagsLink()));
		}

		// display all
		String title = clusterSettingHtml.makeTitle("Tags");
		var content = div(
				clusterSettingHtml.makeHeader(
						title,
						"Tags are stored on the local filesystem and apply to all webapps on the machine"),
				br(),
				html.makeEnabledTagsDiv(),
				br(),
				html.makeTtlDiv(),
				br(),
				html.makeFilesystemPathDiv())
				.withClass("container");
		return pageFactory.simplePage(request, title, content);
	}

}
