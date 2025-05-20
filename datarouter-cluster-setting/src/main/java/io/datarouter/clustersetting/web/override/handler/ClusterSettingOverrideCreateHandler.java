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
import io.datarouter.clustersetting.enums.ClusterSettingScope;
import io.datarouter.clustersetting.link.ClusterSettingBrowseLink;
import io.datarouter.clustersetting.link.ClusterSettingOverrideCreateLink;
import io.datarouter.clustersetting.link.ClusterSettingOverrideViewLink;
import io.datarouter.clustersetting.service.ClusterSettingChangeListener;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.override.ClusterSettingEditSource;
import io.datarouter.clustersetting.web.override.ClusterSettingOverrideForms;
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

public class ClusterSettingOverrideCreateHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClusterSettingPaths paths;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;
	@Inject
	private ClusterSettingOverrideForms forms;
	@Inject
	private DatarouterClusterSettingDao dao;
	@Inject
	private ClusterSettingChangeListener changeListener;
	@Inject
	private DatarouterLinkClient linkClient;

	@Handler
	public Mav create(ClusterSettingOverrideCreateLink overrideCreateLink){
		Optional<String> source = overrideCreateLink.source;
		Optional<String> partialName = overrideCreateLink.partialName;
		Optional<String> name = overrideCreateLink.name;
		Optional<String> scope = overrideCreateLink.scope;
		Optional<String> serverType = overrideCreateLink.serverType;
		Optional<String> serverName = overrideCreateLink.serverName;
		Optional<String> value = overrideCreateLink.value;
		Optional<String> comment = overrideCreateLink.comment;
		Optional<Boolean> submitButton = overrideCreateLink.submitButton;

		String title = clusterSettingHtml.makeTitle("Create Override");
		ClusterSettingScope scopeEnum = ClusterSettingScope.BY_PERSISTENT_STRING.fromOrElse(
				scope.orElse(null),
				ClusterSettingScope.DEFAULT_SCOPE);
		ClusterSettingEditSource sourceEnum = ClusterSettingEditSource.BY_PERSISTENT_STRING.fromOrThrow(
				source.orElse(ClusterSettingEditSource.DATABASE.persistentString));

		//TODO should it check if the setting name exists and link to the standard setting page?

		// Make form
		boolean submitted = submitButton.orElse(false);
		var form = new HtmlForm(HtmlFormMethod.POST)
				.withAction(linkClient.toInternalUrl(new ClusterSettingOverrideCreateLink()));
		form.addHiddenField(ClusterSettingOverrideCreateLink.P_source, sourceEnum.persistentString);
		partialName.ifPresent(partialNameValue -> form.addHiddenField(ClusterSettingOverrideCreateLink.P_partialName,
				partialNameValue));
		form.addField(forms.makeSettingNameField(ClusterSettingOverrideCreateLink.P_name, name, submitted));
		form.addField(forms.makeScopeField(ClusterSettingOverrideCreateLink.P_scope, scopeEnum));
		if(scopeEnum == ClusterSettingScope.SERVER_TYPE){
			form.addField(forms.makeServerTypeField(ClusterSettingOverrideCreateLink.P_serverType, serverName));
		}
		if(scopeEnum == ClusterSettingScope.SERVER_NAME){
			form.addField(
					forms.makeServerNameField(ClusterSettingOverrideCreateLink.P_serverName, serverName, submitted));
		}
		form.addField(forms.makeSettingValueField(ClusterSettingOverrideCreateLink.P_value, value, submitted, name));
		form.addField(forms.makeCommentField(ClusterSettingOverrideCreateLink.P_comment, comment, submitted));
		form.addField(forms.makeSubmitButton(ClusterSettingOverrideCreateLink.P_submitButton, "Create"));

		// Display form
		if(!submitted || form.hasErrors()){
			var htmlForm = Bootstrap4FormHtml.render(form)
					.withClass("card card-body bg-light");
			var formDiv = div(
					h5("Create with comment"),
					htmlForm)
					.withStyle("width:600px;");
			var content = div(
					clusterSettingHtml.makeHeader(title, "Create setting override before the setting exists in code"),
					br(),
					formDiv)
					.withClass("container");
			return pageFactory.simplePage(request, title, content);
		}

		// Save changes
		var clusterSettingKey = new ClusterSettingKey(
				name.orElseThrow(),
				scopeEnum,
				serverType.orElse(ServerType.UNKNOWN.getPersistentString()),
				serverName.orElse(""));
		var clusterSetting = new ClusterSetting(
				clusterSettingKey,
				value.orElseThrow());
		dao.put(clusterSetting);
		changeListener.onCreate(
				clusterSetting,
				getSessionInfo().getRequiredSession().getUsername(),
				comment,
				getUserZoneId());

		// Redirect
		String redirectTo = switch(sourceEnum){
			case DATABASE -> linkClient.toInternalUrl(new ClusterSettingOverrideViewLink());
			case CODE -> linkClient.toInternalUrl(new ClusterSettingBrowseLink()
					.withOptLocation(name)
					.withOptPartialName(partialName));
		};
		return new GlobalRedirectMav(redirectTo);
	}

}
