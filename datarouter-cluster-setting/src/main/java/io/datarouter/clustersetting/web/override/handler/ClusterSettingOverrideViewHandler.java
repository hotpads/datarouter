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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.override.ClusterSettingEditSource;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideHtml;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideTableHtml;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideCreateHandler.ClusterSettingOverrideCreateLinks;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

public class ClusterSettingOverrideViewHandler extends BaseHandler{

	private static final String
			P_partialName = "partialName",
			P_suggestionsOnly = "suggestionsOnly";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClusterSettingOverrideCreateLinks createLinks;
	@Inject
	private ClusterSettingOverrideHtml overrideHtml;
	@Inject
	private ClusterSettingOverrideTableHtml overrideTableHtml;

	@Handler
	public Mav view(
			Optional<String> partialName,
			Optional<Boolean> suggestionsOnly){
		String title = ClusterSettingHtml.makeTitle("Overrides");
		var content = div(
				ClusterSettingHtml.makeHeader(
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
		return div(overrideHtml.makeCreateButton(
				"Create New Override",
				createLinks.create(
						Optional.of(ClusterSettingEditSource.DATABASE),
						optPartialName,
						Optional.empty())));
	}

	private DivTag makeFilterDiv(
			Optional<Boolean> suggestionsOnly,
			Optional<String> partialName){
		var form = new HtmlForm().withMethodGet();
		form.addCheckboxField()
				.withDisplay("Suggestions only")
				.withName(P_suggestionsOnly)
				.withChecked(suggestionsOnly.orElse(false))
				.withSubmitOnChange();
		form.addTextField()
				.withDisplay("Name")
				.withPlaceholder("Partial name")
				.withName(P_partialName)
				.withValue(partialName.orElse(null));
		form.addButtonWithoutSubmitAction()
				.withDisplay("Search");
		return div(
				h5("Search"),
				Bootstrap4FormHtml.render(form, true));
	}

	@Singleton
	public static class ClusterSettingOverrideViewLinks{

		@Inject
		private ServletContextSupplier contextSupplier;
		@Inject
		private DatarouterClusterSettingPaths paths;

		public String view(){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath()
							+ paths.datarouter.settings.overrides.view.toSlashedString());
			return uriBuilder.toString();
		}

		public String view(String partialName){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath()
							+ paths.datarouter.settings.overrides.view.toSlashedString());
			uriBuilder.addParameter(P_partialName, partialName);
			return uriBuilder.toString();
		}

	}

}
