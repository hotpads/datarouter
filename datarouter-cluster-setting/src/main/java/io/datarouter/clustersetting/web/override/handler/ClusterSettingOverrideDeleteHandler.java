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
import io.datarouter.clustersetting.enums.ClusterSettingLogAction;
import io.datarouter.clustersetting.enums.ClusterSettingScope;
import io.datarouter.clustersetting.link.ClusterSettingBrowseLink;
import io.datarouter.clustersetting.link.ClusterSettingOverrideDeleteLink;
import io.datarouter.clustersetting.link.ClusterSettingOverrideViewLink;
import io.datarouter.clustersetting.service.ClusterSettingChangeListener;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.override.ClusterSettingEditSource;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideForms;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideHtml;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class ClusterSettingOverrideDeleteHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClusterSettingPaths paths;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;
	@Inject
	private ClusterSettingOverrideForms forms;
	@Inject
	private ClusterSettingOverrideHtml html;
	@Inject
	private DatarouterClusterSettingDao dao;
	@Inject
	private ClusterSettingChangeListener changeListener;
	@Inject
	private DatarouterLinkClient linkClient;

	@Handler
	public Mav delete(ClusterSettingOverrideDeleteLink deleteLink){
		Optional<String> sourceType = deleteLink.sourceType;
		Optional<String> sourceLocation = deleteLink.sourceLocation;
		Optional<String> partialName = deleteLink.partialName;
		String name = deleteLink.name.orElseThrow();
		Optional<String> serverType = deleteLink.serverType;
		Optional<String> serverName = deleteLink.serverName;
		Optional<String> comment = deleteLink.comment;
		Optional<Boolean> submitButton = deleteLink.submitButton;
		String title = clusterSettingHtml.makeTitle("Delete Setting Override");
		ClusterSettingScope scopeEnum = ClusterSettingScope.fromParams(
				serverType.orElse(null),
				serverName.orElse(null));
		ClusterSettingEditSource sourceEnum = ClusterSettingEditSource.BY_PERSISTENT_STRING.fromOrThrow(
				sourceType.orElse(ClusterSettingEditSource.DATABASE.persistentString));

		// Make form
		boolean submitted = submitButton.orElse(false);
		var form = new HtmlForm(HtmlFormMethod.POST)
				.withAction(linkClient.toInternalUrl(new ClusterSettingOverrideDeleteLink()));
		form.addHiddenField(ClusterSettingOverrideDeleteLink.P_sourceType, sourceEnum.persistentString);
		sourceLocation.ifPresent(
				sourceLocationValue -> form.addHiddenField(ClusterSettingOverrideDeleteLink.P_sourceLocation,
						sourceLocationValue));
		partialName.ifPresent(partialNameValue -> form.addHiddenField(ClusterSettingOverrideDeleteLink.P_partialName,
				partialNameValue));
		form.addHiddenField(ClusterSettingOverrideDeleteLink.P_name, name);
		serverType.ifPresent(serverTypeValue -> form.addHiddenField(ClusterSettingOverrideDeleteLink.P_serverType,
				serverTypeValue));
		serverName.ifPresent(serverNameValue -> form.addHiddenField(ClusterSettingOverrideDeleteLink.P_serverName,
				serverNameValue));
		form.addField(forms.makeCommentField(ClusterSettingOverrideDeleteLink.P_comment, comment, submitted));
		form.addField(forms.makeSubmitButton(ClusterSettingOverrideDeleteLink.P_submitButton, "Delete"));

		// Display form
		if(!submitted || form.hasErrors()){
			var summaryDiv = html.makeSummaryDiv(
					name,
					scopeEnum,
					serverType.orElse(null),
					serverName.orElse(null));
			var htmlForm = Bootstrap4FormHtml.render(form)
					.withClass("card card-body bg-light");
			var formDiv = div(htmlForm)
					.withStyle("width:400px;");
			var content = div(
					clusterSettingHtml.makeHeader(title, "Delete a Setting Override"),
					br(),
					summaryDiv,
					br(),
					h5("Delete with comment"),
					formDiv)
					.withClass("container");
			return pageFactory.simplePage(request, title, content);
		}

		// Save changes
		var clusterSettingKey = new ClusterSettingKey(
				name,
				scopeEnum,
				serverType.orElse(ServerType.UNKNOWN.getPersistentString()),
				serverName.orElse(""));
		ClusterSetting oldSetting = dao.get(clusterSettingKey);
		if(oldSetting == null){
			return pageFactory.message(request, "Setting Override not found.");
		}
		dao.delete(clusterSettingKey);
		changeListener.onUpdateOrDelete(
				oldSetting,
				ClusterSettingLogAction.DELETED,
				getSessionInfo().getRequiredSession().getUsername(),
				comment,
				getUserZoneId());

		// Redirect
		String redirectTo = switch(sourceEnum){
			case DATABASE -> linkClient.toInternalUrl(new ClusterSettingOverrideViewLink());
			case CODE -> linkClient.toInternalUrl(new ClusterSettingBrowseLink()
					.withOptLocation(sourceLocation)
					.withOptPartialName(partialName));
		};
		return new GlobalRedirectMav(redirectTo);
	}

}
