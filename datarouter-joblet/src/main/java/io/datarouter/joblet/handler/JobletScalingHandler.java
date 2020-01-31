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
package io.datarouter.joblet.handler;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.joblet.service.JobletScaler;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

/*
 * note: this is a standalone controller since it requires public access
 */
public class JobletScalingHandler extends BaseHandler{

	public static final Duration HEARTBEAT_WITHIN = Duration.ofMinutes(3);

	@Inject
	private DatarouterWebappInstanceDao webappInstanceDao;
	@Inject
	private JobletScaler jobletScaler;
	@Inject
	private DatarouterProperties datarouterProperties;

	/**
	 * @param jobletServerType serverType of a jobletServer with per-instance and cluster thread limits configured
	 * @return the recommended number of joblet servers to run (in proprietary html format)
	 */
	@Handler(defaultHandler = true)
	public RecommendedJobletServerCountResponse getRecommendedJobletServerCount(String jobletServerType){
		int serverCount = jobletScaler.getNumJobletServers(findJobletWebappInstance(jobletServerType));
		String serverName = datarouterProperties.getServerName();
		return new RecommendedJobletServerCountResponse(serverCount, serverName, new Date());
	}


	// optionally override this in a subclass handler
	protected WebappInstance findJobletWebappInstance(String serverTypeString){
		List<WebappInstance> jobletInstances = webappInstanceDao.getWebappInstancesWithServerTypeString(
				serverTypeString, HEARTBEAT_WITHIN);
		return CollectionTool.getFirst(jobletInstances);
	}

	public static class RecommendedJobletServerCountResponse{
		public final int serverCount;
		public final String serverName;
		public final Date date;

		public RecommendedJobletServerCountResponse(int serverCount, String serverName, Date date){
			this.serverCount = serverCount;
			this.serverName = serverName;
			this.date = date;
		}
	}
}
