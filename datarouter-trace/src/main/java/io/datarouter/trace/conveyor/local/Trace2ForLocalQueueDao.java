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
package io.datarouter.trace.conveyor.local;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.trace.storage.BaseTraceQueueDao;

@Singleton
public class Trace2ForLocalQueueDao extends BaseTraceQueueDao{

	public static class Trace2ForLocalQueueDaoParams extends BaseRedundantDaoParams{

		public Trace2ForLocalQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}
	}

	@Inject
	public Trace2ForLocalQueueDao(Datarouter datarouter,
			Trace2ForLocalQueueDaoParams params, QueueNodeFactory queueNodeFactory){
		super("TraceLocal", datarouter, params, queueNodeFactory);
	}

}
