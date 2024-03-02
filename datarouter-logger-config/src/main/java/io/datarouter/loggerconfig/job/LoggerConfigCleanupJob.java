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
package io.datarouter.loggerconfig.job;

import static j2html.TagCreator.b;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;

import java.time.Duration;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.Level;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.type.DatarouterEmailTypes.LoggerConfigCleanupEmailType;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.loggerconfig.config.DatarouterLoggerConfigSettingRoot;
import io.datarouter.loggerconfig.config.DatarouterLoggingConfigPaths;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao;
import io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig;
import io.datarouter.logging.Log4j2Configurator;
import io.datarouter.storage.config.DatarouterSubscribersSupplier;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.setting.DatarouterEmailSubscriberSettings;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.types.MilliTime;
import io.datarouter.web.config.properties.DefaultEmailDistributionListZoneId;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.PTag;
import jakarta.inject.Inject;

public class LoggerConfigCleanupJob extends BaseJob{

	@Inject
	private DatarouterLoggerConfigDao loggerConfigDao;
	@Inject
	private DatarouterLoggerConfigSettingRoot settings;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private Log4j2Configurator log4j2Configurator;
	@Inject
	private DatarouterLoggingConfigPaths paths;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private LoggerConfigCleanupEmailType loggerConfigCleanupEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private AdminEmail adminEmail;
	@Inject
	private DatarouterSubscribersSupplier subscribers;
	@Inject
	private DatarouterEmailSubscriberSettings subscriberSettings;
	@Inject
	private DefaultEmailDistributionListZoneId defaultEmailDistributionListZoneId;

	@Override
	public void run(TaskTracker tracker){
		loggerConfigDao.scan().forEach(this::handleCustomLogLevel);
	}

	@Deprecated
	private List<String> toAdminsAndSubscribers(){
		List<String> tos = new ArrayList<>();
		tos.add(adminEmail.get());
		if(subscriberSettings.includeSubscribers.get()){
			tos.addAll(subscribers.get());
		}
		return tos;
	}

	private void handleCustomLogLevel(LoggerConfig log){
		MilliTime lastUpdatedThreshold = MilliTime.now().minus(settings.loggingConfigMaxAgeDays.get().longValue(),
				ChronoUnit.DAYS);
		MilliTime loggerLastUpdatedDate = log.getLastUpdated();
		if(loggerLastUpdatedDate.isAfter(lastUpdatedThreshold)){
			return;
		}

		boolean handleLoggerConfigDeletionAlerts = settings.handleLoggerConfigDeletionAlerts.get();
		if(!handleLoggerConfigDeletionAlerts && settings.sendLoggerConfigCleanupJobEmails.get()){
			List<String> toEmails = new ArrayList<>();
			if(serverTypeDetector.mightBeProduction()){
				toEmails.addAll(toAdminsAndSubscribers());
				toEmails.addAll(loggerConfigCleanupEmailType.tos);
			}
			sendAlertEmail(toEmails, log, makeDefaultOldLoggerConfigDetails(log));
			return;
		}

		Level databaseLoggerLevel = log.getLevel().level;
		Level rootLoggerLevel = log4j2Configurator.getRootLoggerLevel();
		if(databaseLoggerLevel.isMoreSpecificThan(rootLoggerLevel) && settings.sendLoggerConfigCleanupJobEmails.get()){
			sendAlertEmail(List.of(), log, makeLoggerLevelAlertDetails(log, rootLoggerLevel));
		}
		int daysSinceLastUpdatedThreshold = (int) Duration.ofMillis(lastUpdatedThreshold.minus(loggerLastUpdatedDate)
				.toEpochMilli())
				.toDays();
		int daysLeftBeforeDeletingLogger = settings.loggingConfigSendEmailAlertDays.get()
				- daysSinceLastUpdatedThreshold;
		if(daysLeftBeforeDeletingLogger <= 0){
			loggerConfigDao.delete(log.getKey());
			var dto = new DatarouterChangelogDtoBuilder(
					"LoggerConfig",
					log.getKey().getName(),
					"delete",
					log.getEmail())
					.sendEmail()
					.excludeMainDatarouterAdmin()
					.excludeSubscribers()
					.build();
			changelogRecorder.record(dto);
			if(settings.sendLoggerConfigCleanupJobEmails.get()){
				sendAlertEmail(toAdminsAndSubscribers(), log, makeDeleteLoggerConfigAlertDetails(log));
			}
			return;
		}
		if(settings.sendLoggerConfigCleanupJobEmails.get()){
			sendAlertEmail(List.of(), log, makeOldLoggerConfigAlertDetails(log, daysLeftBeforeDeletingLogger));
		}
	}

	private PTag makeDefaultOldLoggerConfigDetails(LoggerConfig log){
		return p(
				text("The LoggerConfig named "),
				b(log.getKey().getName()),
				text(" was last updated on "),
				b(log.getLastUpdated() + ""),
				text(" so it's older than the maximum age threshold of "),
				b(settings.loggingConfigMaxAgeDays.get() + ""),
				text(" days."),
				br(),
				text("Either the LoggerConfig should be deleted or the code updated."));
	}

	private PTag makeLoggerLevelAlertDetails(LoggerConfig log, Level rootLoggerLevel){
		return p(
				text("The LoggerConfig "),
				b(log.getKey().getName()),
				text(" has a level of "),
				text(log.getLevel() + ""),
				text(" which either overrides the root logger level "),
				b(rootLoggerLevel + ""),
				text(" which might lead to a flood of logs when it eventually gets automatically deleted,"),
				text(" or is redundant if the two levels are equal."));
	}

	private PTag makeOldLoggerConfigAlertDetails(LoggerConfig log, int daysLeft){
		return p(
				text("The LoggerConfig "),
				b(log.getKey().getName()),
				text(" was last updated on "),
				text(log.getLastUpdated() + ""),
				text(" so it's older than the maximum age threshold of "),
				b(settings.loggingConfigMaxAgeDays.get() + ""),
				text(" days."),
				br(),
				text("This LoggerConfig will be automatically deleted in "),
				b(daysLeft + ""),
				text(" days if not updated."));
	}

	private PTag makeDeleteLoggerConfigAlertDetails(LoggerConfig log){
		ZoneId zoneId = defaultEmailDistributionListZoneId.get();
		return p(
				text("The LoggerConfig "),
				b(log.getKey().getName()),
				text(" was last updated on "),
				b(log.getLastUpdated().format(zoneId)),
				text(" so it's older than the maximum age threshold of "),
				b(settings.loggingConfigMaxAgeDays.get() + ""),
				text(" days."),
				br(),
				text("This LoggerConfig has been automatically deleted after "),
				b(settings.loggingConfigSendEmailAlertDays.get() + ""),
				text(" days of alerts."));
	}

	private void sendAlertEmail(Collection<String> toEmails, LoggerConfig log, PTag details){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.logging)
				.build();
		var content = makeEmailContent(details);
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Logger Config")
				.withTitleHref(primaryHref)
				.withContent(content)
				.fromAdmin()
				.to(toEmails)
				.to(log.getEmail());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private DivTag makeEmailContent(PTag details){
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		var description = h3("Old LoggerConfig alert from:");
		var detailsHeader = h4("Details:");
		return div(header, description, detailsHeader, details);
	}

}
