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
package io.datarouter.clustersetting.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.text;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.datarouter.clustersetting.config.DatarouterClusterSettingRoot;
import io.datarouter.clustersetting.enums.ClusterSettingLogAction;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog;
import io.datarouter.email.email.DatarouterEmailLinkBuilder;
import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.email.type.DatarouterEmailTypes.ClusterSettingEmailType;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService.HtmlEmailHeaderRow;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingEmailService{

	@Inject
	private DatarouterClusterSettingRoot settings;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private DatarouterWebPaths datarouterWebPaths;
	@Inject
	private DatarouterHtmlEmailService datarouterHtmlEmailService;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private ClusterSettingEmailType clusterSettingEmailType;

	public void sendEmail(
			ClusterSettingLog log,
			String oldValue,
			Optional<String> username,
			ZoneId userZoneId){
		if(!settings.sendUpdateEmail.get()){
			return;
		}
		if(username.isEmpty()){
			return;
		}
		String title = "Setting Update";
		String primaryHref = completeLink(datarouterHtmlEmailService.startLinkBuilder(), log)
				.build();
		boolean displayValue = !settings.isExcludedOldSettingString(log.getKey().getName());
		J2HtmlDatarouterEmailBuilder emailBuilder = datarouterHtmlEmailService.startEmailBuilder()
				.withTitle(title)
				.withTitleHref(primaryHref)
				.withContent(new ClusterSettingChangeEmailContent(log, oldValue, displayValue, userZoneId).build())
				.fromAdmin()
				.to(clusterSettingEmailType.tos, serverTypeDetector.mightBeProduction())
				.to(username.get());
		datarouterHtmlEmailService.trySendJ2Html(emailBuilder);
	}

	private DatarouterEmailLinkBuilder completeLink(DatarouterEmailLinkBuilder linkBuilder, ClusterSettingLog log){
		return linkBuilder
				.withLocalPath(datarouterWebPaths.datarouter.settings)
				.withParam("submitAction", "browseSettings")
				.withParam("name", log.getKey().getName());
	}

	private class ClusterSettingChangeEmailContent{
		private final ClusterSettingLog log;
		private final String oldValue;
		private final boolean displayValue;
		private final ZoneId userZoneId;

		public ClusterSettingChangeEmailContent(
				ClusterSettingLog log,
				String oldValue,
				boolean displayValue,
				ZoneId userZoneId){
			this.log = log;
			this.oldValue = oldValue;
			this.displayValue = displayValue;
			this.userZoneId = userZoneId;
		}

		private DivTag build(){
			List<HtmlEmailHeaderRow> kvs = new ArrayList<>();
			kvs.add(new HtmlEmailHeaderRow("user", text(log.getChangedBy())));
			kvs.add(new HtmlEmailHeaderRow("action", text(log.getAction().persistentString)));
			kvs.add(new HtmlEmailHeaderRow("setting", makeClusterSettingLogLink()));
			String timestamp = ZonedDateFormatterTool.formatReversedLongMsWithZone(
					log.getKey().getReverseCreatedMs(),
					userZoneId);
			kvs.add(new HtmlEmailHeaderRow("timestamp", text(timestamp)));
			if(ObjectTool.notEquals(ServerType.UNKNOWN.getPersistentString(), log.getServerType())){
				kvs.add(new HtmlEmailHeaderRow("serverType", text(log.getServerType())));
			}
			if(StringTool.notEmpty(log.getServerName())){
				kvs.add(new HtmlEmailHeaderRow("serverName", text(log.getServerName())));
			}
			if(displayValue){
				if(ClusterSettingLogAction.INSERTED != log.getAction()){
					kvs.add(new HtmlEmailHeaderRow("old value", text(oldValue)));
				}
				if(ClusterSettingLogAction.DELETED != log.getAction()){
					kvs.add(new HtmlEmailHeaderRow("new value", text(log.getValue())));
				}
			}
			String comment = StringTool.notNullNorEmptyNorWhitespace(log.getComment())
					? log.getComment()
					: "No comment provided";
			kvs.add(new HtmlEmailHeaderRow("comment", text(comment)));
			return standardDatarouterEmailHeaderService.makeStandardHeaderWithSupplementsHtml(kvs);
		}

		private ATag makeClusterSettingLogLink(){
			return a(log.getKey().getName())
					.withHref(completeLink(datarouterHtmlEmailService.startLinkBuilder(), log)
							.build());
		}

	}
}
