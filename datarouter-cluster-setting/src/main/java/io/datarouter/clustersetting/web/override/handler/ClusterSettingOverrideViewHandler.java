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
package io.datarouter.clustersetting.web.override.handler;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;

import java.util.Optional;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.link.ClusterSettingOverrideCreateLink;
import io.datarouter.clustersetting.link.ClusterSettingOverrideViewLink;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.override.ClusterSettingEditSource;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideHtml;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideTableHtml;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class ClusterSettingOverrideViewHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;
	@Inject
	private DatarouterLinkClient linkClient;
	@Inject
	private ClusterSettingOverrideHtml overrideHtml;
	@Inject
	private ClusterSettingOverrideTableHtml overrideTableHtml;

	@Handler
	public Mav view(ClusterSettingOverrideViewLink viewLink){

		Optional<String> partialName = viewLink.partialName;
		Optional<Boolean> suggestionsOnly = viewLink.suggestionsOnly;
		String title = clusterSettingHtml.makeTitle("Overrides");
		var content = div(
				clusterSettingHtml.makeHeader(
						title,
						"Overrides are stored in the database and take precedence over values defined in the code"),
				br(),
				makeCreateButtonDiv(partialName),
				br(),
				makeFilterDiv(suggestionsOnly, partialName),
				br(),
				overrideTableHtml.makeTablesDiv(partialName, suggestionsOnly.orElse(false)))
				.withClass("container");
		return pageFactory.simplePage(request, title, content);
	}

	private DivTag makeCreateButtonDiv(Optional<String> optPartialName){
		var clusterOverrideCreateLink = new ClusterSettingOverrideCreateLink()
				.withSource(ClusterSettingEditSource.DATABASE)
				.withOptPartialName(optPartialName);

		return div(overrideHtml.makeCreateButton(
				"Create New Override",
				linkClient.toInternalUrl(clusterOverrideCreateLink)));
	}

	private DivTag makeFilterDiv(
			Optional<Boolean> suggestionsOnly,
			Optional<String> partialName){
		var form = new HtmlForm(HtmlFormMethod.GET);
		form.addCheckboxField()
				.withLabel("Suggestions only")
				.withName(ClusterSettingOverrideViewLink.P_suggestionsOnly)
				.withChecked(suggestionsOnly.orElse(false))
				.withSubmitOnChange();
		form.addTextField()
				.withLabel("Name")
				.withPlaceholder("Partial name")
				.withName(ClusterSettingOverrideViewLink.P_partialName)
				.withValue(partialName.orElse(null));
		form.addButtonWithoutSubmitAction()
				.withLabel("Search");
		return div(
				h5("Search"),
				Bootstrap4FormHtml.render(form, true));
	}

	@Singleton
	public static class ClusterSettingOverrideEmailLinks{

		@Inject
		private DatarouterHtmlEmailService emailService;
		@Inject
		private DatarouterClusterSettingPaths paths;

		public String view(String partialName){
			return emailService.startLinkBuilder()
					.withLocalPath(paths.datarouter.settings.overrides.view)
					.withParam(ClusterSettingOverrideViewLink.P_partialName, partialName)
					.build();
		}

	}

}
