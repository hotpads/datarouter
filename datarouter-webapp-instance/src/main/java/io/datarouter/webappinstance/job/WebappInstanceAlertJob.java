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
package io.datarouter.webappinstance.job;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.text;

import java.time.Instant;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.email.StandardDatarouterEmailHeaderService;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.email.type.DatarouterEmailTypes.WebappInstanceAlertEmailType;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.time.DurationTool;
import io.datarouter.util.time.ZonedDateFormaterTool;
import io.datarouter.util.tuple.Twin;
import io.datarouter.webappinstance.config.DatarouterWebappInstancePaths;
import io.datarouter.webappinstance.config.DatarouterWebappInstanceSettingRoot;
import io.datarouter.webappinstance.service.WebappInstanceService;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class WebappInstanceAlertJob extends BaseJob{

	@Inject
	private WebappInstanceService webappInstanceService;
	@Inject
	private DatarouterWebappInstanceSettingRoot settings;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterAdministratorEmailService additionalAdministratorEmailService;
	@Inject
	private DatarouterWebappInstancePaths paths;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private WebappInstanceAlertEmailType webappInstanceAlertEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;

	@Override
	public void run(TaskTracker tracker){
		WebappInstance webappInstance = webappInstanceService.buildCurrentWebappInstance();
		Instant build = webappInstance.getBuildInstant();
		long staleRunningTimeMs = DurationTool.sinceInstant(build).toMillis()
				- settings.staleWebappInstanceThreshold.get().toJavaDuration().toMillis();
		if(staleRunningTimeMs > 0){
			sendEmail(webappInstance);
		}
	}

	private void sendEmail(WebappInstance webappInstance){
		String from = datarouterProperties.getAdministratorEmail();
		String to;
		if(serverTypeDetector.mightBeProduction()){
			to = webappInstanceAlertEmailType.getAsCsv(additionalAdministratorEmailService
					.getAdministratorEmailAddresses());
		}else{
			to = datarouterProperties.getAdministratorEmail();
		}
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.webappInstances)
				.build();
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject("Datarouter - Stale Webapp - " + webappInstance.getKey().getServerName())
				.withTitle("Stale Webapp")
				.withTitleHref(primaryHref)
				.withContent(body(header, makeContent(webappInstance)));
		htmlEmailService.trySendJ2Html(from, to, emailBuilder);
	}

	private ContainerTag makeContent(WebappInstance webappInstance){
		var rows = List.of(
				new Twin<>("webapp", webappInstance.getKey().getWebappName()),
				new Twin<>("build date", ZonedDateFormaterTool.formatInstantWithZone(
						webappInstance.getBuildInstant(),
						datarouterService.getZoneId())),
				new Twin<>("startup date", ZonedDateFormaterTool.formatInstantWithZone(
						webappInstance.getStartupInstant(),
						datarouterService.getZoneId())),
				new Twin<>("commitId", webappInstance.getCommitId()));
		return new J2HtmlEmailTable<Twin<String>>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> text(row.getRight())))
				.build(rows);
	}

	private static DomContent makeDivBoldRight(String text){
		return div(text).withStyle("font-weight:bold;text-align:right;");
	}

}
