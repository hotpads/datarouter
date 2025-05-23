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
package io.datarouter.web.handler;

import static j2html.TagCreator.body;
import static j2html.TagCreator.pre;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.type.DatarouterEmailTypes.SchemaUpdatesEmailType;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.relay.dto.RelayStartThreadRequestDto;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.relay.DatarouterRelaySenderProvider;
import io.datarouter.relay.DatarouterRelayTopics;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.schema.BaseSchemaUpdateService;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.settings.DatarouterSchemaUpdateEmailSettings;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import jakarta.inject.Provider;

public abstract class EmailingSchemaUpdateService extends BaseSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(EmailingSchemaUpdateService.class);

	private static final String FROM_NAME = "Schema Update";

	private final DatarouterHtmlEmailService htmlEmailService;
	private final DatarouterWebPaths datarouterWebPaths;
	private final StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	private final SchemaUpdatesEmailType schemaUpdatesEmailType;
	private final DatarouterSchemaUpdateEmailSettings schemaUpdateEmailSettings;
	private final DatarouterRelaySenderProvider relaySenderProvider;
	private final DatarouterRelayTopics relayTopics;

	public EmailingSchemaUpdateService(
			ServerName serverName,
			EnvironmentName environmentName,
			AdminEmail adminEmail,
			DatarouterSchemaUpdateScheduler executor,
			Provider<DatarouterClusterSchemaUpdateLockDao> schemaUpdateLockDao,
			Provider<ChangelogRecorder> changelogRecorder,
			String buildId,
			DatarouterHtmlEmailService htmlEmailService,
			DatarouterWebPaths datarouterWebPaths,
			StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService,
			SchemaUpdatesEmailType schemaUpdatesEmailType,
			DatarouterSchemaUpdateEmailSettings schemaUpdateEmailSettings,
			DatarouterRelaySenderProvider relaySenderProvider,
			DatarouterRelayTopics relayTopics){
		super(serverName, environmentName, adminEmail, executor, schemaUpdateLockDao, changelogRecorder, buildId);
		this.htmlEmailService = htmlEmailService;
		this.datarouterWebPaths = datarouterWebPaths;
		this.standardDatarouterEmailHeaderService = standardDatarouterEmailHeaderService;
		this.schemaUpdatesEmailType = schemaUpdatesEmailType;
		this.schemaUpdateEmailSettings = schemaUpdateEmailSettings;
		this.relaySenderProvider = relaySenderProvider;
		this.relayTopics = relayTopics;
	}

	@Override
	protected void sendEmail(String subject, String body){
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(datarouterWebPaths.datarouter)
				.build();
		String title = "Schema Update";
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(subject)
				.withTitle(title)
				.withTitleHref(primaryHref)
				.withContent(body(header, pre(body)))
				.from("SchemaUpdate <" + adminEmail.get() + ">")
				.to(schemaUpdatesEmailType.tos)
				.toSubscribers()
				.toAdmin(schemaUpdateEmailSettings.sendToAdmin.get());
		logger.warn("Sending Schema update email from Admin with subject={}", subject);
		htmlEmailService.trySendJ2Html(emailBuilder);
		logger.warn("Sending relay message with topics={}", relayTopics.schemaUpdateServiceStartup());
		try{
			relaySenderProvider.get().startThreadForceProduction(new RelayStartThreadRequestDto(
					relayTopics.schemaUpdateServiceStartup(),
					FROM_NAME,
					subject,
					Rml.doc(
							Rml.heading(1, Rml.text(title).link(primaryHref)),
							Rml.paragraph(Rml.text("Differences identified between databean schema and table schema."
									+ " The following updates would align the table schema with the databean schema:")),
							Rml.codeBlock(body.trim()))));
		}catch(Exception e){
			logger.error("Failed to send relay message", e);
		}
	}

}
