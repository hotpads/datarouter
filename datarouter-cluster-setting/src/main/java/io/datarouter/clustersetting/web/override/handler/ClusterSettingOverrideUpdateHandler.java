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

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.enums.ClusterSettingLogAction;
import io.datarouter.clustersetting.enums.ClusterSettingScope;
import io.datarouter.clustersetting.link.ClusterSettingBrowseLink;
import io.datarouter.clustersetting.link.ClusterSettingOverrideUpdateLink;
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
import j2html.TagCreator;
import jakarta.inject.Inject;

public class ClusterSettingOverrideUpdateHandler extends BaseHandler{

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
	public Mav update(ClusterSettingOverrideUpdateLink updateLink){
		Optional<String> source = updateLink.source;
		Optional<String> partialName = updateLink.partialName;
		String name = updateLink.name.orElseThrow();
		Optional<String> serverType = updateLink.serverType;
		Optional<String> serverName = updateLink.serverName;
		Optional<String> value = updateLink.value;
		Optional<String> comment = updateLink.comment;
		Optional<Boolean> submitButton = updateLink.submitButton;

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
				.withAction(linkClient.toInternalUrl(new ClusterSettingOverrideUpdateLink()));
		form.addHiddenField(ClusterSettingOverrideUpdateLink.P_source, sourceEnum.persistentString);
		partialName.ifPresent(partialNameValue -> form.addHiddenField(ClusterSettingOverrideUpdateLink.P_partialName,
				partialNameValue));
		form.addHiddenField(ClusterSettingOverrideUpdateLink.P_name, name);
		serverType.ifPresent(
				serverTypeValue -> form.addHiddenField(ClusterSettingOverrideUpdateLink.P_serverType,
						serverTypeValue));
		serverName.ifPresent(
				serverNameValue -> form.addHiddenField(ClusterSettingOverrideUpdateLink.P_serverName,
						serverNameValue));
		form.addField(forms.makeSettingValueField(
				ClusterSettingOverrideUpdateLink.P_value,
				value.isPresent() ? value : Optional.of(setting.getValue()),
				submitted,
				Optional.of(name)));
		form.addField(forms.makeCommentField(ClusterSettingOverrideUpdateLink.P_comment, comment, submitted));
		form.addField(forms.makeSubmitButton(ClusterSettingOverrideUpdateLink.P_submitButton, "Update"));

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
			case DATABASE -> linkClient.toInternalUrl(new ClusterSettingOverrideViewLink());
			case CODE -> linkClient.toInternalUrl(new ClusterSettingBrowseLink()
					.withLocation(name)
					.withOptPartialName(partialName));
		};
		return new GlobalRedirectMav(redirectTo);
	}

}
