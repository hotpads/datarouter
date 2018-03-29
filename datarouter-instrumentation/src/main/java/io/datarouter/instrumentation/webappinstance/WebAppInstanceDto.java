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

public class WebAppInstanceDto{

	public final String webAppName;
	public final String serverName;
	public final String serverType;
	public final String servletContextPath;
	public final String serverPublicIp;
	public final Date refreshedLast;
	public final Date startupDate;
	public final Date buildDate;
	public final String commitId;

	public WebAppInstanceDto(String webAppName, String serverName, String serverType, String servletContextPath,
			String serverPublicIp, Date refreshedLast, Date startupDate, Date buildDate, String commitId){
		this.webAppName = webAppName;
		this.serverName = serverName;
		this.serverType = serverType;
		this.servletContextPath = servletContextPath;
		this.serverPublicIp = serverPublicIp;
		this.refreshedLast = refreshedLast;
		this.startupDate = startupDate;
		this.buildDate = buildDate;
		this.commitId = commitId;
	}

}
