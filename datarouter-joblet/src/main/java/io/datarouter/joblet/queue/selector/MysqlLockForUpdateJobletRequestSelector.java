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
package io.datarouter.joblet.queue.selector;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.jdbc.GetJobletRequest;
import io.datarouter.joblet.jdbc.JobletRequestSqlBuilder;
import io.datarouter.joblet.queue.JobletRequestQueueManager;
import io.datarouter.joblet.queue.JobletRequestSelector;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequestqueue.JobletRequestQueueKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.Datarouter;
import io.datarouter.util.timer.PhaseTimer;

@Singleton
public class MysqlLockForUpdateJobletRequestSelector implements JobletRequestSelector{

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private MysqlFieldCodecFactory mysqlFieldCodecFactory;
	@Inject
	private JobletRequestSqlBuilder jobletRequestSqlBuilder;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private DatarouterJobletCounters datarouterJobletCounters;
	@Inject
	private SessionExecutor sessionExecutor;
	@Inject
	private MysqlSqlFactory mysqlSqlFactory;
	@Inject
	private JobletService jobletService;

	@Override
	public Optional<JobletRequest> getJobletRequestForProcessing(
			PhaseTimer timer,
			JobletType<?> type,
			String reservedBy){
		while(true){
			var mysqlOp = new GetJobletRequest(reservedBy, type, datarouter, jobletRequestDao, mysqlFieldCodecFactory,
					mysqlSqlFactory, jobletRequestSqlBuilder, jobletService);
			JobletRequest jobletRequest = sessionExecutor.runWithoutRetries(mysqlOp);
			timer.add("GetJobletRequest");
			if(jobletRequest == null){
				jobletRequestQueueManager.onJobletRequestMissForAllPriorities(type);//for back-off
				return Optional.empty();
			}
			if(!jobletRequest.getStatus().isRunning()){
				continue;// weird flow. it was probably just marked as timedOut, so skip it
			}
			var queueKey = new JobletRequestQueueKey(type, jobletRequest.getKey().getPriority());
			datarouterJobletCounters.incQueueHit(queueKey.getQueueName());
			return Optional.of(jobletRequest);
		}
	}

}
