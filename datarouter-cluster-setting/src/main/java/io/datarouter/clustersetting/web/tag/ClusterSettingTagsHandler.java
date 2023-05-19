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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.storage.setting.cached.CachedClusterSettingTags;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;

public class ClusterSettingTagsHandler extends BaseHandler{

	public static final String
			P_tagName = "tagName",
			P_tagEnabled = "tagEnabled";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CachedClusterSettingTags cachedClusterSettingTags;
	@Inject
	private ClusterSettingTagsLinks clusterSettingTagLinks;
	@Inject
	private ClusterSettingTagsHtml html;

	@Handler
	public Mav tags(
			Optional<String> tagName,
			Optional<Boolean> tagEnabled){

		// toggle tag and redirect
		if(tagName.isPresent()){
			cachedClusterSettingTags.updateTag(tagName.orElseThrow(), tagEnabled.orElse(false));
			return new GlobalRedirectMav(clusterSettingTagLinks.tags());
		}

		// display all
		String title = ClusterSettingHtml.makeTitle("Tags");
		var content = div(
				ClusterSettingHtml.makeHeader(
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

	@Singleton
	public static class ClusterSettingTagsLinks{

		@Inject
		private ServletContextSupplier contextSupplier;
		@Inject
		private DatarouterClusterSettingPaths paths;

		public String tags(){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath() + paths.datarouter.settings.tags.toSlashedString());
			return uriBuilder.toString();
		}

	}

}
