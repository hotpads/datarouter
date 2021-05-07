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
package io.datarouter.web.handler;

import static j2html.TagCreator.pre;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.config.schema.BaseSchemaUpdateService;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao;
import io.datarouter.web.config.DatarouterWebPaths;

public abstract class EmailingSchemaUpdateService extends BaseSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(EmailingSchemaUpdateService.class);

	private final DatarouterHtmlEmailService htmlEmailService;
	private final DatarouterWebPaths datarouterWebPaths;

	public EmailingSchemaUpdateService(DatarouterProperties datarouterProperties,
			DatarouterAdministratorEmailService adminEmailService,
			DatarouterSchemaUpdateScheduler executor,
			Provider<DatarouterClusterSchemaUpdateLockDao> schemaUpdateLockDao,
			Provider<ChangelogRecorder> changelogRecorder,
			String buildId,
			DatarouterHtmlEmailService htmlEmailService,
			DatarouterWebPaths datarouterWebPaths){
		super(datarouterProperties, adminEmailService, executor, schemaUpdateLockDao, changelogRecorder, buildId);
		this.htmlEmailService = htmlEmailService;
		this.datarouterWebPaths = datarouterWebPaths;
	}

	@Override
	protected void sendEmail(String fromEmail, String toEmail, String subject, String body){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(datarouterWebPaths.datarouter)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(subject)
				.withTitle("Schema Update")
				.withTitleHref(primaryHref)
				.withContent(pre(body));
		htmlEmailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
		logger.warn("Sending Schema update email from={}, with subject={}", fromEmail, subject);
	}

}
