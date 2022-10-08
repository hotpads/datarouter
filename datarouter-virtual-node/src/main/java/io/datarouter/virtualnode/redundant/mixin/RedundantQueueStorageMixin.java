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
package io.datarouter.virtualnode.redundant.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.QueueStorage;
import io.datarouter.storage.node.op.raw.QueueStorage.QueueStorageNode;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.virtualnode.redundant.RedundantQueueNode;

public interface RedundantQueueStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends QueueStorageNode<PK,D,F>>
extends QueueStorage<PK,D>, RedundantQueueNode<PK,D,F,N>{

	@Override
	default D poll(Config config){
		return Scanner.of(getReadNodes())
				.map(node -> node.poll(config))
				.findFirst()
				.orElse(null);
	}

	@Override
	default List<D> pollMulti(Config config){
		return Scanner.of(getReadNodes())
				.concatIter(node -> node.pollMulti(config))
				.list();
	}

	@Override
	default Scanner<D> pollUntilEmpty(Config config){
		return Scanner.of(getReadNodes())
				.concat(node -> node.pollUntilEmpty(config));
	}

	@Override
	default void ack(QueueMessageKey key, Config config){
		var phaseTimer = new PhaseTimer();
		for(N node : getReadNodes()){
			try{
				node.ack(key, config);
				phaseTimer.add("success " + node);
				return;
			}catch(RuntimeException e){
				RedundantQueueNodeTool.swallowIfNotFound(e, node);
				phaseTimer.add("failed node " + node);
			}
		}
	}

	@Override
	default void ackMulti(Collection<QueueMessageKey> keys, Config config){
		var phaseTimer = new PhaseTimer();
		for(N node : getReadNodes()){
			try{
				node.ackMulti(keys, config);
				phaseTimer.add("success " + node);
				return;
			}catch(RuntimeException e){
				RedundantQueueNodeTool.swallowIfNotFound(e, node);
				phaseTimer.add("failed node " + node);
			}
		}
	}

	@Override
	default void put(D databean, Config config){
		getWriteNode().put(databean, config);
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		getWriteNode().putMulti(databeans, config);
	}

	@Override
	default QueueMessage<PK,D> peek(Config config){
		var phaseTimer = new PhaseTimer();
		return Scanner.of(getReadNodes())
				.map(node -> {
					QueueMessage<PK,D> databean = node.peek(config);
					phaseTimer.add("node " + node);
					return databean;
				}).include(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	@Override
	default List<QueueMessage<PK,D>> peekMulti(Config config){
		var phaseTimer = new PhaseTimer();
		return Scanner.of(getReadNodes())
				.map(node -> {
					List<QueueMessage<PK,D>> messages = node.peekMulti(config);
					phaseTimer.add("node " + node);
					return messages;
				}).exclude(Collection::isEmpty)
				.findFirst()
				.orElseGet(List::of);
	}

	@Override
	default Scanner<QueueMessage<PK,D>> peekUntilEmpty(Config config){
		return Scanner.of(getReadNodes())
				.concat(node -> node.peekUntilEmpty(config));
	}

}
