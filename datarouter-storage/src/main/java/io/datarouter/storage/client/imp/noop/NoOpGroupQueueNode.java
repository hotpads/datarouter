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
package io.datarouter.storage.client.imp.noop;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.GroupQueueStorage;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class NoOpGroupQueueNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NoOpNode<PK,D>
implements GroupQueueStorage<PK,D>{

	@Override
	public void ack(QueueMessageKey key, Config config){
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
	}

	@Override
	public GroupQueueMessage<PK,D> peek(Config config){
		return null;
	}

	@Override
	public List<GroupQueueMessage<PK,D>> peekMulti(Config config){
		return Collections.emptyList();
	}

	@Override
	public Iterable<GroupQueueMessage<PK,D>> peekUntilEmpty(Config config){
		return Collections.emptyList();
	}

	@Override
	public List<D> pollMulti(Config config){
		return Collections.emptyList();
	}

}
