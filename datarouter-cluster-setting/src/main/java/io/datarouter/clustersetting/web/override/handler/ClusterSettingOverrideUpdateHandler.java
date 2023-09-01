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

import java.util.Optional;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.enums.ClusterSettingLogAction;
import io.datarouter.clustersetting.enums.ClusterSettingScope;
import io.datarouter.clustersetting.service.ClusterSettingChangeListener;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseHandlerParams;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseLinks;
import io.datarouter.clustersetting.web.override.ClusterSettingEditSource;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideForms;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideHtml;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideViewHandler.ClusterSettingOverrideViewLinks;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class ClusterSettingOverrideUpdateHandler extends BaseHandler{

	private static final String
			P_source = "source",
			P_partialName = "partialName",
			P_name = "name",
			P_serverType = "serverType",
			P_serverName = "serverName",
			P_value = "value",
			P_comment = "comment",
			P_submitButton = "submitButton";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClusterSettingPaths paths;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;
	@Inject
	private ClusterSettingBrowseLinks browseLinks;
	@Inject
	private ClusterSettingOverrideViewLinks overrideLinks;
	@Inject
	private ClusterSettingOverrideForms forms;
	@Inject
	private ClusterSettingOverrideHtml html;
	@Inject
	private DatarouterClusterSettingDao dao;
	@Inject
	private ClusterSettingChangeListener changeListener;

	@Handler
	public Mav update(
			Optional<String> source,
			Optional<String> partialName,
			String name,
			Optional<String> serverType,
			Optional<String> serverName,
			Optional<String> value,
			Optional<String> comment,
			Optional<Boolean> submitButton){
		String title = clusterSettingHtml.makeTitle("Update Override");
		ClusterSettingScope scopeEnum = ClusterSettingScope.fromParams(
				serverType.orElse(null),
				serverName.orElse(null));
		ClusterSettingEditSource sourceEnum = ClusterSettingEditSource.BY_PERSISTENT_STRING.fromOrThrow(
				source.orElse(ClusterSettingEditSource.DATABASE.persistentString));
		var settingKey = new ClusterSettingKey(
				name,
				scopeEnum,
				serverType.orElse(ServerType.UNKNOWN.getPersistentString()),
				serverName.orElse(""));
		ClusterSetting setting = dao.get(settingKey);

		// Make form
		boolean submitted = submitButton.orElse(false);
		var form = new HtmlForm(HtmlFormMethod.POST)
				.withAction(request.getContextPath() + paths.datarouter.settings.overrides.update.toSlashedString());
		form.addHiddenField(P_source, sourceEnum.persistentString);
		partialName.ifPresent(partialNameValue -> form.addHiddenField(P_partialName, partialNameValue));
		form.addHiddenField(P_name, name);
		serverType.ifPresent(serverTypeValue -> form.addHiddenField(P_serverType, serverTypeValue));
		serverName.ifPresent(serverNameValue -> form.addHiddenField(P_serverName, serverNameValue));
		form.addField(forms.makeSettingValueField(
				P_value,
				value.isPresent() ? value : Optional.of(setting.getValue()),
				submitted,
				Optional.of(name)));
		form.addField(forms.makeCommentField(P_comment, comment, submitted));
		form.addField(forms.makeSubmitButton(P_submitButton, "Update"));

		// Display form
		if(!submitted || form.hasErrors()){
			var summaryDiv = html.makeSummaryDiv(
					name,
					scopeEnum,
					serverType.orElse(null),
					serverName.orElse(null));
			var htmlForm = Bootstrap4FormHtml.render(form)
					.withClass("card card-body bg-light");
			var formDiv = div(
					TagCreator.h5("Update with comment"),
					htmlForm)
					.withStyle("width:400px;");
			var content = div(
					clusterSettingHtml.makeHeader(title, "Update Setting Override Value"),
					br(),
					summaryDiv,
					br(),
					formDiv)
					.withClass("container");
			return pageFactory.simplePage(request, title, content);
		}

		// Save changes
		setting.setValue(value.orElseThrow());
		dao.put(setting);
		changeListener.onUpdateOrDelete(
				setting,
				ClusterSettingLogAction.UPDATED,
				getSessionInfo().getRequiredSession().getUsername(),
				comment,
				getUserZoneId());

		// Redirect
		String redirectTo = switch(sourceEnum){
			case DATABASE -> overrideLinks.view();
			case CODE -> browseLinks.all(new ClusterSettingBrowseHandlerParams()
					.withLocation(name)
					.withOptPartialName(partialName));
		};
		return new GlobalRedirectMav(redirectTo);
	}

	@Singleton
	public static class ClusterSettingOverrideUpdateLinks{

		@Inject
		private ServletContextSupplier contextSupplier;
		@Inject
		private DatarouterClusterSettingPaths paths;

		public String update(
				Optional<ClusterSettingEditSource> optSource,
				Optional<String> optPartialName,
				String name,
				Optional<String> optServerType,
				Optional<String> optServerName){
			var uriBuilder = new URIBuilder()
					.setPath(contextSupplier.getContextPath()
							+ paths.datarouter.settings.overrides.update.toSlashedString());
			optSource.ifPresent(source -> uriBuilder.addParameter(P_source, source.persistentString));
			optPartialName.ifPresent(partialName -> uriBuilder.addParameter(P_partialName, partialName));
			uriBuilder.addParameter(P_name, name);
			optServerType.ifPresent(serverType -> uriBuilder.addParameter(P_serverType, serverType));
			optServerName.ifPresent(serverName -> uriBuilder.addParameter(P_serverName, serverName));
			return uriBuilder.toString();
		}

	}

}
