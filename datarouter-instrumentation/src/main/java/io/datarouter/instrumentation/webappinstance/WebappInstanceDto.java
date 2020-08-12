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
package io.datarouter.instrumentation.webappinstance;

import java.util.Date;

public class WebappInstanceDto{

	public final String webappName;
	public final String serverName;
	public final String serverType;
	public final String servletContextPath;
	public final String serverPublicIp;
	public final String serverPrivateIp;
	public final Date refreshedLast;
	public final Date startupDate;
	public final Date buildDate;
	public final String buildId;
	public final String commitId;
	public final String javaVersion;
	public final String servletContainerVersion;
	public final String gitBranch;

	public WebappInstanceDto(
			String webappName,
			String serverName,
			String serverType,
			String servletContextPath,
			String serverPublicIp,
			String serverPrivateIp,
			Date refreshedLast,
			Date startupDate,
			Date buildDate,
			String buildId,
			String commitId,
			String javaVersion,
			String servletContainerVersion,
			String gitBranch){
		this.webappName = webappName;
		this.serverName = serverName;
		this.serverType = serverType;
		this.servletContextPath = servletContextPath;
		this.serverPublicIp = serverPublicIp;
		this.serverPrivateIp = serverPrivateIp;
		this.refreshedLast = refreshedLast;
		this.startupDate = startupDate;
		this.buildDate = buildDate;
		this.buildId = buildId;
		this.commitId = commitId;
		this.javaVersion = javaVersion;
		this.servletContainerVersion = servletContainerVersion;
		this.gitBranch = gitBranch;
	}

}
