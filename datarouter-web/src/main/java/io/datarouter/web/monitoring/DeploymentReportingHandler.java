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
package io.datarouter.web.monitoring;

import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import io.datarouter.web.handler.BaseHandler;

public class DeploymentReportingHandler extends BaseHandler{

	@Inject
	private GitProperties gitProperties;

	@Handler(defaultHandler = true)
	public DeploymentReportDto deploymentReport(){
		return new DeploymentReportDto(
				gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING),
				gitProperties.getBranch().orElse(GitProperties.UNKNOWN_STRING),
				gitProperties.getCommitUserName().orElse(GitProperties.UNKNOWN_STRING),
				DateTimeFormatter.ISO_INSTANT.format(gitProperties.getBuildTime().orElse(GitProperties.UNKNOWN_DATE)));
	}

	public static class DeploymentReportDto{

		public final String gitCommit;
		public final String gitBranch;
		public final String gitUsername;
		public final String buildDate;

		public DeploymentReportDto(String gitCommit, String gitBranch, String gitUsername, String buildDate){
			this.gitCommit = gitCommit;
			this.gitBranch = gitBranch;
			this.gitUsername = gitUsername;
			this.buildDate = buildDate;
		}

	}

}
