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
package io.datarouter.storage.client.imp.noop;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.QueueStorage;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class NoOpQueueNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NoOpNode<PK,D>
implements QueueStorage<PK,D>{

	@Override
	public void ack(QueueMessageKey key, Config config){
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
	}

	@Override
	public QueueMessage<PK,D> peek(Config config){
		return null;
	}

	@Override
	public List<QueueMessage<PK,D>> peekMulti(Config config){
		return List.of();
	}

	@Override
	public Scanner<QueueMessage<PK,D>> peekUntilEmpty(Config config){
		return Scanner.empty();
	}

	@Override
	public D poll(Config config){
		return null;
	}

	@Override
	public List<D> pollMulti(Config config){
		return List.of();
	}

	@Override
	public Scanner<D> pollUntilEmpty(Config config){
		return Scanner.empty();
	}

}
