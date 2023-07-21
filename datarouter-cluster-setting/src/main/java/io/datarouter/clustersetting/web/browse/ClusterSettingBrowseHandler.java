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

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseNavHtml.ClusterSettingBrowseNavHtmlFactory;
import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkType.NoOpLinkType;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
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

	public static class ClusterSettingBrowseHandlerParams extends BaseLink<NoOpLinkType>{

		private static final String
				P_location = "location",
				P_partialName = "partialName";

		public Optional<String> location = Optional.empty();
		public Optional<String> partialName = Optional.empty();

		public ClusterSettingBrowseHandlerParams(){
			super(new DatarouterClusterSettingPaths().datarouter.settings.browse.all);
		}

		// location
		public ClusterSettingBrowseHandlerParams withLocation(String location){
			this.location = Optional.of(location);
			return this;
		}

		public ClusterSettingBrowseHandlerParams withOptLocation(Optional<String> optLocation){
			this.location = optLocation;
			return this;
		}

		// partialName
		public ClusterSettingBrowseHandlerParams withPartialName(String partialName){
			this.partialName = Optional.of(partialName);
			return this;
		}

		public ClusterSettingBrowseHandlerParams withOptPartialName(Optional<String> optPartialName){
			this.partialName = optPartialName;
			return this;
		}
	}

	@Handler
	public Mav all(ClusterSettingBrowseHandlerParams params){
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

	private DivTag makeFilterDiv(ClusterSettingBrowseHandlerParams params){
		var form = new HtmlForm().withMethodGet();
		params.location.ifPresent(location -> form.addHiddenField(
				ClusterSettingBrowseHandlerParams.P_location,
				location));
		form.addTextField()
				.withDisplay("Name")
				.withPlaceholder("Partial name")
				.withName(ClusterSettingBrowseHandlerParams.P_partialName)
				.withValue(params.partialName.orElse(null));
		form.addButtonWithoutSubmitAction()
				.withDisplay("Search");
		return div(
				Bootstrap4FormHtml.render(form, true));
	}

	// TODO replace by generic BaseLink link builder
	@Singleton
	public static class ClusterSettingBrowseLinks{
		@Inject
		private ServletContextSupplier contextSupplier;

		public String all(ClusterSettingBrowseHandlerParams params){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath() + params.pathNode.toSlashedString());
			params.location.ifPresent(location -> uriBuilder.addParameter(
					ClusterSettingBrowseHandlerParams.P_location,
					location));
			params.partialName.ifPresent(partialName -> uriBuilder.addParameter(
					ClusterSettingBrowseHandlerParams.P_partialName,
					partialName));
			return uriBuilder.toString();
		}
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
					.withParam(ClusterSettingBrowseHandlerParams.P_location, location)
					.build();
		}
	}

}
