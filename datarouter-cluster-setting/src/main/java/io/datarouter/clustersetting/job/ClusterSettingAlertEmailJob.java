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
package io.datarouter.clustersetting.job;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.clustersetting.config.DatarouterClusterSettingRoot;
import io.datarouter.clustersetting.service.ClusterSettingDailyDigest;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.relay.DatarouterRelayJ2HtmlRenderTool;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;

/**
 * Similar to the DailyDigest. This job emails about old, unreferenced, and redundant cluster settings.
 */
public class ClusterSettingAlertEmailJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(ClusterSettingAlertEmailJob.class);

	@Inject
	private ClusterSettingDailyDigest dailyDigest;
	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterClusterSettingRoot settings;
	@Inject
	private EnvironmentName environmentName;
	@Inject
	private ServiceName serviceName;

	@Override
	public void run(TaskTracker tracker){
		Optional<RmlBlock> content = dailyDigest.getRelayContent(ZoneIds.AMERICA_LOS_ANGELES);
		if(content.isEmpty()){
			logger.warn("no settings to alert on");
			return;
		}
		var email = emailService.startEmailBuilder()
				.withSubject(String.format("Cluster Setting Alert - %s - %s", environmentName.get(), serviceName.get()))
				.withTitle("Cluster Setting Alert")
				.withContent(DatarouterRelayJ2HtmlRenderTool.render(content.get().build()))
				.fromAdmin()
				.toSubscribers()
				.to(settings.alertJobRecipients.get());
		emailService.trySendJ2Html(email);
	}

}
