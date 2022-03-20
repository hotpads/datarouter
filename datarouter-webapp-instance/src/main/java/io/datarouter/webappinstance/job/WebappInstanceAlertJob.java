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
import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.email.type.DatarouterEmailTypes.WebappInstanceAlertEmailType;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.util.tuple.Twin;
import io.datarouter.web.config.properties.DefaultEmailDistributionListZoneId;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
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
	private DatarouterWebappInstancePaths paths;
	@Inject
	private WebappInstanceAlertEmailType webappInstanceAlertEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private DefaultEmailDistributionListZoneId defaultDistributionListZoneId;

	@Override
	public void run(TaskTracker tracker){
		WebappInstance webappInstance = webappInstanceService.buildCurrentWebappInstance();
		Instant build = webappInstance.getBuildInstant();
		DatarouterDuration buildAge = DatarouterDuration.age(build);
		if(buildAge.isLongerThan(settings.staleWebappInstanceThreshold.get())){
			sendEmail(webappInstance, buildAge);
		}
	}

	private void sendEmail(WebappInstance webappInstance, DatarouterDuration buildAge){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.webappInstances)
				.build();
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject("Datarouter - Stale Webapp - " + webappInstance.getKey().getServerName())
				.withTitle("Stale Webapp")
				.withTitleHref(primaryHref)
				.withContent(body(header, makeContent(webappInstance, buildAge)))
				.fromAdmin()
				.to(webappInstanceAlertEmailType.tos, serverTypeDetector.mightBeProduction())
				.toAdmin()
				.toSubscribers(serverTypeDetector.mightBeProduction());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private ContainerTag<?> makeContent(WebappInstance webappInstance, DatarouterDuration buildAge){
		ZoneId zoneId = defaultDistributionListZoneId.get();
		var rows = List.of(
				new Twin<>("webapp", webappInstance.getKey().getWebappName()),
				new Twin<>("build date", ZonedDateFormatterTool.formatInstantWithZone(
						webappInstance.getBuildInstant(),
						zoneId)),
				new Twin<>("build age", buildAge.toString()),
				new Twin<>("startup date", ZonedDateFormatterTool.formatInstantWithZone(
						webappInstance.getStartupInstant(),
						zoneId)),
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
