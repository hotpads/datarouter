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
package io.datarouter.trace.storage;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;

@Singleton
public class Trace2ForLocalDao extends BaseTrace2Dao{

	public static class Trace2ForLocalDaoParams extends BaseDaoParams{

		public Trace2ForLocalDaoParams(ClientId clientId){
			super(clientId);
		}
	}

	@Inject
	public Trace2ForLocalDao(
			Datarouter datarouter,
			Trace2ForLocalDaoParams params,
			NodeFactory nodeFactory){
		super(Optional.empty(), datarouter, params, nodeFactory);
	}

}
