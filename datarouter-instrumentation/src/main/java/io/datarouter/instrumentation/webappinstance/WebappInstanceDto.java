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
package io.datarouter.instrumentation.webappinstance;

import java.time.Instant;
import java.util.Date;

public record WebappInstanceDto(
		String webappName,
		String serverName,
		String serverType,
		String serviceName,
		String servletContextPath,
		String serverPublicIp,
		String serverPrivateIp,
		Instant refreshedLastInstant,
		Instant startup,
		Instant build,
		String buildId,
		String commitId,
		String javaVersion,
		String servletContainerVersion,
		String gitBranch,
		Integer httpsPort,
		@Deprecated Date startupDate,
		@Deprecated Date buildDate,
		@Deprecated Date refreshedLast){

	public WebappInstanceDto(
			String webappName,
			String serverName,
			String serverType,
			String serviceName,
			String servletContextPath,
			String serverPublicIp,
			String serverPrivateIp,
			Instant refreshedLastInstant,
			Instant startup,
			Instant build,
			String buildId,
			String commitId,
			String javaVersion,
			String servletContainerVersion,
			String gitBranch,
			Integer httpsPort){
		this(webappName,
				serverName,
				serverType,
				serviceName,
				servletContextPath,
				serverPublicIp,
				serverPrivateIp,
				refreshedLastInstant,
				startup,
				build,
				buildId,
				commitId,
				javaVersion,
				servletContainerVersion,
				gitBranch,
				httpsPort,
				null,
				null,
				null);
	}

	public Instant refreshedLastInstant(){
		return refreshedLastInstant != null ? refreshedLastInstant : refreshedLast.toInstant();
	}

	public Instant startup(){
		return startup != null ? startup : startupDate.toInstant();
	}

	public Instant build(){
		return build != null ? build : buildDate.toInstant();
	}

}
