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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

public class ClusterSettingBrowseHandler extends BaseHandler{

	public static final String
			P_location = "location",
			P_partialName = "partialName";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ClusterSettingBrowseNavHtml navHtml;

	/*------------ handlers ------------*/

	@Handler
	public Mav all(
			Optional<String> location,
			Optional<String> partialName){
		String title = ClusterSettingHtml.makeTitle("Browse");
		var headerDiv = ClusterSettingHtml.makeHeader(title, "Browse settings via the hierarchy defined in code");
		var filterDiv = makeFilterDiv(location, partialName);
		var treeDiv = navHtml.makeBodyDiv(location, partialName);
		var content = div(
				headerDiv,
				br(),
				filterDiv,
				treeDiv)
				.withClass("container");
		return pageFactory.simplePage(request, title, content);
	}

	private DivTag makeFilterDiv(
			Optional<String> optLocation,
			Optional<String> optPartialName){
		var form = new HtmlForm().withMethodGet();
		optLocation.ifPresent(location -> form.addHiddenField(P_location, location));
		form.addTextField()
				.withDisplay("Name")
				.withPlaceholder("Partial name")
				.withName(P_partialName)
				.withValue(optPartialName.orElse(null));
		form.addButtonWithoutSubmitAction()
				.withDisplay("Search");
		return div(
				Bootstrap4FormHtml.render(form, true));
	}

	/*----------- links ------------*/

	@Singleton
	public static class ClusterSettingBrowseLinks{

		@Inject
		private ServletContextSupplier contextSupplier;
		@Inject
		private DatarouterClusterSettingPaths paths;

		public String all(
				Optional<String> optLocation,
				Optional<String> optPartialName){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath() + paths.datarouter.settings.browse.all.toSlashedString());
			optLocation.ifPresent(location -> uriBuilder.addParameter(P_location, location));
			optPartialName.ifPresent(partialName -> uriBuilder.addParameter(P_partialName, partialName));
			return uriBuilder.toString();
		}

	}

}
