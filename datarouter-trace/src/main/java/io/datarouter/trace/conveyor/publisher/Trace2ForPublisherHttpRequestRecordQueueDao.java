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
package io.datarouter.trace.conveyor.publisher;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.trace.storage.BaseTrace2HttpRequestRecordQueueDao;

@Singleton
public class Trace2ForPublisherHttpRequestRecordQueueDao extends BaseTrace2HttpRequestRecordQueueDao{

	public static class Trace2ForPublisherHttpRequestRecordQueueDaoParams extends BaseRedundantDaoParams{

		public Trace2ForPublisherHttpRequestRecordQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}
	}

	@Inject
	public Trace2ForPublisherHttpRequestRecordQueueDao(Datarouter datarouter,
			Trace2ForPublisherHttpRequestRecordQueueDaoParams params, QueueNodeFactory queueNodeFactory){
		super("TracePublisherHttpRequestRecord", datarouter, params, queueNodeFactory);
	}

}
