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
package io.datarouter.clustersetting.web.browse;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.link.ClusterSettingBrowseLink;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseNavHtml.ClusterSettingBrowseNavHtmlFactory;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class ClusterSettingBrowseHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClusterSettingBrowseNavHtmlFactory navHtmlFactory;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;

	@Handler
	public Mav all(ClusterSettingBrowseLink params){
		String title = clusterSettingHtml.makeTitle("Browse");
		var headerDiv = clusterSettingHtml.makeHeader(title, "Browse settings via the hierarchy defined in code");
		var filterDiv = makeFilterDiv(params);
		var navHtml = navHtmlFactory.create(params);
		var treeDiv = navHtml.makeBodyDiv();
		var content = div(
				headerDiv,
				br(),
				filterDiv,
				treeDiv)
				.withClass("container");
		return pageFactory.simplePage(request, title, content);
	}

	private DivTag makeFilterDiv(ClusterSettingBrowseLink params){
		var form = new HtmlForm(HtmlFormMethod.GET);
		params.location.ifPresent(location -> form.addHiddenField(
				ClusterSettingBrowseLink.P_location,
				location));
		form.addTextField()
				.withLabel("Name")
				.withPlaceholder("Partial name")
				.withName(ClusterSettingBrowseLink.P_partialName)
				.withValue(params.partialName.orElse(null));
		form.addButtonWithoutSubmitAction()
				.withLabel("Search");
		return div(
				Bootstrap4FormHtml.render(form, true));
	}

	@Singleton
	public static class ClusterSettingBrowseEmailLinks{
		@Inject
		private DatarouterClusterSettingPaths paths;
		@Inject
		private DatarouterHtmlEmailService emailService;

		public String fromEmail(String location){
			return emailService.startLinkBuilder()
					.withLocalPath(paths.datarouter.settings.browse.all)
					.withParam(ClusterSettingBrowseLink.P_location, location)
					.build();
		}
	}

}
