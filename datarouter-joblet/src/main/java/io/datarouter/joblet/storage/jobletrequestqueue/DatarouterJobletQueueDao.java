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
package io.datarouter.joblet.storage.jobletrequestqueue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.DatarouterJobletConstants;
import io.datarouter.joblet.queue.JobletRequestQueueManager;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest.JobletRequestFielder;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.QueueStorage;
import io.datarouter.storage.tag.Tag;

@Singleton
public class DatarouterJobletQueueDao extends BaseDao{

	public static class DatarouterJobletQueueDaoParams extends BaseRedundantDaoParams{

		public DatarouterJobletQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final Map<JobletRequestQueueKey,QueueStorage<JobletRequestKey,JobletRequest>> jobletRequestQueueByKey;

	@Inject
	public DatarouterJobletQueueDao(
			Datarouter datarouter,
			DatarouterJobletQueueDaoParams params,
			JobletRequestQueueManager jobletRequestQueueManager,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		jobletRequestQueueByKey = Scanner.of(jobletRequestQueueManager.getQueueKeys())
				.toMap(Function.identity(),
						queueKey -> {
							String nodeName = DatarouterJobletConstants.QUEUE_PREFIX + queueKey.getQueueName();
							return queueNodeFactory.createSingleQueue(
									params.clientIds.get(0),
									JobletRequest::new,
									JobletRequestFielder::new)
									.withQueueName(nodeName)
									.withTag(Tag.DATAROUTER)
									.buildAndRegister();
						});
	}

	public void put(JobletRequestQueueKey queueKey, JobletRequest request){
		jobletRequestQueueByKey.get(queueKey).put(request);
	}

	public QueueStorage<JobletRequestKey,JobletRequest> getQueue(JobletRequestQueueKey queueKey){
		return jobletRequestQueueByKey.get(queueKey);
	}

}
