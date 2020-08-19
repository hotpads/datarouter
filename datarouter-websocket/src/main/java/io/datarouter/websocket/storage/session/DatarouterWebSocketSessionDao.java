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
package io.datarouter.websocket.storage.session;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.util.tuple.Range;
import io.datarouter.websocket.storage.session.WebSocketSession.WebSocketSessionFielder;

@Singleton
public class DatarouterWebSocketSessionDao extends BaseDao{

	public static class DatarouterWebSocketDaoParams extends BaseDaoParams{

		public DatarouterWebSocketDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<WebSocketSessionKey,WebSocketSession> node;

	@Inject
	public DatarouterWebSocketSessionDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterWebSocketDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, WebSocketSession::new, WebSocketSessionFielder::new)
				.disableNodewatchPercentageAlert()
				.withIsSystemTable(true)
				.buildAndRegister();
	}

	public void put(WebSocketSession databean){
		node.put(databean);
	}

	public void delete(WebSocketSessionKey key){
		node.delete(key);
	}

	public void deleteMulti(Collection<WebSocketSessionKey> keys){
		node.deleteMulti(keys);
	}

	public Optional<WebSocketSession> find(WebSocketSessionKey key){
		return node.find(key);
	}

	public List<WebSocketSession> getMulti(Collection<WebSocketSessionKey> keys){
		return node.getMulti(keys);
	}

	public Scanner<WebSocketSession> scan(){
		return node.scan();
	}

	public Scanner<WebSocketSession> scanWithPrefix(WebSocketSessionKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public long count(Range<WebSocketSessionKey> range){
		return node.scanKeys(range).count();
	}

}