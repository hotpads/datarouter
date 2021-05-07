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
package io.datarouter.webappinstance.storage.webappinstance;

import java.time.Instant;

public class WebappInstance extends BaseWebappInstance<WebappInstanceKey,WebappInstance>{

	public static class WebappInstanceFielder extends BaseWebappInstanceFielder<WebappInstanceKey,WebappInstance>{

		public WebappInstanceFielder(){
			super(WebappInstanceKey.class);
		}

	}

	public WebappInstance(){
		super(new WebappInstanceKey());
	}

	public WebappInstance(String webappName, String serverName, String serverType){
		super(new WebappInstanceKey(webappName, serverName),
				serverType,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null);
	}

	// refreshedLastInstant is Instant.now() -- webapp update job assumes this is happening
	public WebappInstance(
			String webappName,
			String serverName,
			String serverType,
			String servletContextPath,
			String publicIp,
			String privateIp,
			Instant startup,
			Instant build,
			String buildId,
			String commitId,
			String javaVersion,
			String servletContainerVersion,
			String gitBranch,
			Integer httpsPort){
		super(new WebappInstanceKey(webappName, serverName),
				serverType,
				servletContextPath,
				publicIp,
				privateIp,
				Instant.now(),
				startup,
				build,
				buildId,
				commitId,
				javaVersion,
				servletContainerVersion,
				gitBranch,
				httpsPort);
	}

	@Override
	public Class<WebappInstanceKey> getKeyClass(){
		return WebappInstanceKey.class;
	}

}
