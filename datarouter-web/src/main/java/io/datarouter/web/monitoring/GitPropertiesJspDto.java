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
package io.datarouter.web.monitoring;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class GitPropertiesJspDto{

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
			.ofPattern("d MMM H:mm z")
			.withZone(ZoneId.systemDefault());

	private final String buildTime;
	private final String commitTime;
	private final String describeShort;
	private final String branch;
	private final String idAbbrev;
	private final String commitUserName;

	public GitPropertiesJspDto(GitProperties gitProperties){
		this.buildTime = FORMATTER.format(gitProperties.getBuildTime().orElse(GitProperties.UNKNOWN_DATE));
		this.commitTime = FORMATTER.format(gitProperties.getCommitTime().orElse(GitProperties.UNKNOWN_DATE));
		this.describeShort = gitProperties.getDescribeShort().orElse(GitProperties.UNKNOWN_STRING);
		this.branch = gitProperties.getBranch().orElse(GitProperties.UNKNOWN_STRING);
		this.idAbbrev = gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING);
		this.commitUserName = gitProperties.getCommitUserName().orElse(GitProperties.UNKNOWN_STRING);
	}

	public String getBuildTime(){
		return buildTime;
	}

	public String getCommitTime(){
		return commitTime;
	}

	public String getDescribeShort(){
		return describeShort;
	}

	public String getBranch(){
		return branch;
	}

	public String getIdAbbrev(){
		return idAbbrev;
	}

	public String getCommitUserName(){
		return commitUserName;
	}

}
