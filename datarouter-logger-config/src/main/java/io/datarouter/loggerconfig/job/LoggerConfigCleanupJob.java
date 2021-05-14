/**
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
package io.datarouter.loggerconfig.job;

import static j2html.TagCreator.b;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.loggerconfig.config.DatarouterLoggerConfigSettingRoot;
import io.datarouter.loggerconfig.config.DatarouterLoggingConfigPaths;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao;
import io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig;
import io.datarouter.logging.Log4j2Configurator;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.DateTool;
import io.datarouter.util.tuple.Twin;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class LoggerConfigCleanupJob extends BaseJob{

	@Inject
	private DatarouterLoggerConfigDao loggerConfigDao;
	@Inject
	private DatarouterLoggerConfigSettingRoot settings;
	@Inject
	private DatarouterAdministratorEmailService adminEmailService;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private Log4j2Configurator log4j2Configurator;
	@Inject
	private DatarouterLoggingConfigPaths paths;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private ChangelogRecorder changelogRecorder;

	private int maxAgeLimitDays;
	private int loggingConfigSendEmailAlertDays;

	@Override
	public void run(TaskTracker tracker){
		maxAgeLimitDays = settings.loggingConfigMaxAgeDays.get();
		loggingConfigSendEmailAlertDays = settings.loggingConfigSendEmailAlertDays.get();
		loggerConfigDao.scan()
				.forEach(this::handleCustomLogLevel);
	}

	private void handleCustomLogLevel(LoggerConfig log){
		Date lastUpdatedThreshold = DateTool.getDaysAgo(maxAgeLimitDays);
		Date loggerLastUpdatedDate = log.getLastUpdated();
		if(loggerLastUpdatedDate.after(lastUpdatedThreshold)){
			return;
		}

		boolean handleLoggerConfigDeletionAlerts = settings.handleLoggerConfigDeletionAlerts.get();
		if(!handleLoggerConfigDeletionAlerts && settings.sendLoggerConfigCleanupJobEmails.get()){
			String toEmails = adminEmailService.getAdministratorEmailAddressesCsv(log.getEmail());
			sendAlertEmail(toEmails, makeDefaultOldLoggerConfigDetails(log));
			return;
		}

		Level databaseLoggerLevel = log.getLevel().getLevel();
		Level rootLoggerLevel = log4j2Configurator.getRootLoggerLevel();
		if(databaseLoggerLevel.isMoreSpecificThan(rootLoggerLevel)
				&& settings.sendLoggerConfigCleanupJobEmails.get()){
			sendAlertEmail(log.getEmail(), makeLoggerLevelAlertDetails(log, rootLoggerLevel));
		}

		int daysSinceLastUpdatedThreshold = DateTool.getDatesBetween(loggerLastUpdatedDate, lastUpdatedThreshold);
		int daysLeftBeforeDeletingLogger = loggingConfigSendEmailAlertDays - daysSinceLastUpdatedThreshold;
		if(daysLeftBeforeDeletingLogger <= 0){
			loggerConfigDao.delete(log.getKey());
			var dto = new DatarouterChangelogDtoBuilder("LoggerConfig", log.getKey().getName(), "delete", "cleanup job")
					.sendEmail()
					.excludeMainDatarouterAdmin()
					.excludeAdditionalAdministrators()
					.build();
			changelogRecorder.record(dto);
			if(settings.sendLoggerConfigCleanupJobEmails.get()){
				String toEmails = adminEmailService.getAdministratorEmailAddressesCsv(log.getEmail());
				sendAlertEmail(toEmails, makeDeleteLoggerConfigAlertDetails(log));
			}
			return;
		}
		if(settings.sendLoggerConfigCleanupJobEmails.get()){
			String toEmails = log.getEmail();
			sendAlertEmail(toEmails, makeOldLoggerConfigAlertDetails(log, daysLeftBeforeDeletingLogger));
		}
	}

	private ContainerTag makeDefaultOldLoggerConfigDetails(LoggerConfig log){
		return p(
				text("The LoggerConfig named "),
				b(log.getName()),
				text(" was last updated on "),
				b(log.getLastUpdated() + ""),
				text(" so it's older than the maximum age threshold of "),
				b(maxAgeLimitDays + ""),
				text(" days."),
				br(),
				text("Either the LoggerConfig should be deleted or the code updated."));
	}

	private ContainerTag makeLoggerLevelAlertDetails(LoggerConfig log, Level rootLoggerLevel){
		return p(
				text("The LoggerConfig "),
				b(log.getName()),
				text(" has a level of "),
				text(log.getLevel() + ""),
				text(" which either overrides the root logger level "),
				b(rootLoggerLevel + ""),
				text(" which might lead to a flood of logs when it eventually gets automatically deleted,"),
				text(" or is redundant if the two levels are equal."));
	}

	private ContainerTag makeOldLoggerConfigAlertDetails(LoggerConfig log, int daysLeft){
		return p(
				text("The LoggerConfig "),
				b(log.getName()),
				text(" was last updated on "),
				text(log.getLastUpdated() + ""),
				text(" so it's older than the maximum age threshold of "),
				b(maxAgeLimitDays + ""),
				text(" days."),
				br(),
				text("This LoggerConfig will be automatically deleted in "),
				b(daysLeft + ""),
				text(" days if not updated."));
	}

	private ContainerTag makeDeleteLoggerConfigAlertDetails(LoggerConfig log){
		return p(
				text("The LoggerConfig "),
				b(log.getName()),
				text(" was last updated on "),
				b(DateTool.formatDateWithZone(log.getLastUpdated(), datarouterService.getZoneId())),
				text(" so it's older than the maximum age threshold of "),
				b(maxAgeLimitDays + ""),
				text(" days."),
				br(),
				text("This LoggerConfig has been automatically deleted after "),
				b(loggingConfigSendEmailAlertDays + ""),
				text(" days of alerts."));
	}

	private void sendAlertEmail(String toEmails, ContainerTag details){
		String fromEmail = datarouterProperties.getAdministratorEmail();
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.logging)
				.build();
		var content = makeEmailContent(details);
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Logger Config")
				.withTitleHref(primaryHref)
				.withContent(content);
		htmlEmailService.trySendJ2Html(fromEmail, toEmails, emailBuilder);
	}

	private ContainerTag makeEmailContent(ContainerTag details){
		var header = h3("Old LoggerConfig alert from:");
		var table = new J2HtmlEmailTable<Twin<String>>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> text(row.getRight())))
				.build(makeEmailTableValues());
		var detailsHeader = h4("Details:");
		return div(header, table, detailsHeader, details);
	}

	private List<Twin<String>> makeEmailTableValues(){
		return List.of(
				Twin.of("environment", datarouterProperties.getEnvironment()),
				Twin.of("service", datarouterService.getServiceName()),
				Twin.of("host", datarouterProperties.getServerName()));
	}

	private static DomContent makeDivBoldRight(String text){
		return div(text).withStyle("font-weight:bold;text-align:right;");
	}

}
