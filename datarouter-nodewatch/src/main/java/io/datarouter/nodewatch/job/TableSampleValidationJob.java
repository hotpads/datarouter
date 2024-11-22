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
package io.datarouter.nodewatch.job;

import static j2html.TagCreator.body;
import static j2html.TagCreator.p;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.KvString;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.email.type.DatarouterEmailTypes.SchemaUpdatesEmailType;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import j2html.tags.specialized.BodyTag;
import jakarta.inject.Inject;

public class TableSampleValidationJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(TableSampleValidationJob.class);

	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private ServiceName serviceName;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private AdminEmail adminEmail;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private SchemaUpdatesEmailType schemaUpdatesEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private NodewatchLinks nodewatchLinks;

	@Override
	public void run(TaskTracker tracker){
		List<String> invalidNames = tableSamplerService.scanCountableNodes()
				.exclude(tableSamplerService::checkAllSamplesParseable)
				.map(node -> String.format(
						"%s-%s",
						node.clientAndTableNames().client(),
						node.clientAndTableNames().table()))
				.list();
		if(!invalidNames.isEmpty()){
			logInvalidTables(invalidNames);
			emailInvalidTables(invalidNames);
		}
	}

	private void logInvalidTables(List<String> invalidNames){
		logger.warn("invalidSamples {}", new KvString()
				.add("service", serviceName.get())
				.add("tables", String.join(", ", invalidNames)));
	}

	private void emailInvalidTables(List<String> invalidNames){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(nodewatchLinks.tables())
				.build();
		J2HtmlDatarouterEmailBuilder emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Invalid Table Samples")
				.withTitleHref(primaryHref)
				.withContent(makeEmailContent(invalidNames))
				.from(adminEmail.get())
				.to(serverTypeDetector.mightBeProduction() ? schemaUpdatesEmailType.tos : Set.of(adminEmail.get()));
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private BodyTag makeEmailContent(List<String> invalidNames){
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		String invalidNamesStr = String.join(", ", invalidNames);
		String message = String.format("Invalid Table Samples found in %s", invalidNamesStr);
		return body(header, p(message));
	}

}
