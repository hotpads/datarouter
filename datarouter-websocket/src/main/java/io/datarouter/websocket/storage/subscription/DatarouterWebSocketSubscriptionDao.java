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
package io.datarouter.websocket.storage.subscription;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.IndexingNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.index.IndexReader;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantIndexedSortedMapStorageNode;
import io.datarouter.websocket.storage.subscription.WebSocketSubscription.WebSocketSubscriptionFielder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebSocketSubscriptionDao extends BaseDao{

	public record DatarouterWebSocketSubscriptionDaoParams(List<ClientId> clientIds){
	}

	private final IndexedSortedMapStorageNode<
			WebSocketSubscriptionKey,
			WebSocketSubscription,
			WebSocketSubscriptionFielder> node;

	private final IndexReader<
			WebSocketSubscriptionKey,
			WebSocketSubscription,
			WebSocketSubscriptionByUserTokenKey,
			FieldlessIndexEntry<
					WebSocketSubscriptionByUserTokenKey,
					WebSocketSubscriptionKey,
					WebSocketSubscription>> byToken;

	@Inject
	public DatarouterWebSocketSubscriptionDao(Datarouter datarouter, NodeFactory nodeFactory,
			IndexingNodeFactory indexingNodeFactory, DatarouterWebSocketSubscriptionDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					IndexedSortedMapStorageNode<
							WebSocketSubscriptionKey,
							WebSocketSubscription,
							WebSocketSubscriptionFielder> node = nodeFactory.create(
									clientId,
									WebSocketSubscription::new,
									WebSocketSubscriptionFielder::new)
							.withTag(Tag.DATAROUTER)
							.disableNodewatchPercentageAlert()
							.build();
					return node;
				})
				.listTo(RedundantIndexedSortedMapStorageNode::makeIfMulti);
		byToken = indexingNodeFactory.createKeyOnlyManagedIndex(WebSocketSubscriptionByUserTokenKey::new, node)
				.build();
		datarouter.register(node);
	}

	public Scanner<WebSocketSubscriptionKey> scanKeys(){
		return node.scanKeys();
	}

	public Scanner<WebSocketSubscriptionKey> scanKeys(Range<WebSocketSubscriptionKey> range){
		return node.scanKeys(range);
	}

	public Scanner<WebSocketSubscriptionByUserTokenKey> scanKeysWithPrefixByUserToken(
			WebSocketSubscriptionByUserTokenKey prefix){
		return byToken.scanKeysWithPrefix(prefix);
	}

	public Scanner<WebSocketSubscriptionKey> scanKeysWithPrefix(WebSocketSubscriptionKey prefix){
		return node.scanKeysWithPrefix(prefix);
	}

	public void put(WebSocketSubscription databean){
		node.put(databean);
	}

	public void delete(WebSocketSubscriptionKey key){
		node.delete(key);
	}

	public void deleteMulti(Collection<WebSocketSubscriptionKey> keys){
		node.deleteMulti(keys);
	}

}
