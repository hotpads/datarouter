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
package io.datarouter.webappinstance.job;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.job.storage.clusterjoblock.ClusterJobLock;
import io.datarouter.job.storage.clusterjoblock.DatarouterClusterJobLockDao;
import io.datarouter.web.app.WebappName;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogKey;
import jakarta.inject.Inject;

public class DeadClusterJobLockVacuumJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(DeadClusterJobLockVacuumJob.class);

	private static final Duration DEADLINE = Duration.ofMinutes(30);

	@Inject
	private DatarouterWebappInstanceLogDao webappInstanceLogDao;
	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterClusterJobLockDao clusterJobLockDao;

	@Override
	public void run(TaskTracker tracker){
		Map<String,Boolean> deadServers = new HashMap<>();
		clusterJobLockDao.scan()
				.include(lock -> deadServers.computeIfAbsent(lock.getServerName(), this::isDeadServer))
				.each(lock -> logger.warn("Unlocking job={} serverName={}", lock.getKey().getJobName(),
						lock.getServerName()))
				.map(ClusterJobLock::getKey)
				.batch(100)
				.forEach(clusterJobLockDao::deleteMulti);
	}

	private boolean isDeadServer(String serverName){
		var prefix = new WebappInstanceLogKey(webappName.getName(), serverName, null, null);
		return webappInstanceLogDao.scanWithPrefix(prefix)
				.map(WebappInstanceLog::getRefreshedLast)
				.findMax(Comparator.naturalOrder())
				.map(refreshedLast -> refreshedLast.plus(DEADLINE).isBefore(Instant.now()))
				.orElse(true);
	}

}
