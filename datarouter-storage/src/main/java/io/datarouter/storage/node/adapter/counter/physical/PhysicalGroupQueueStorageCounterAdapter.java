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
package io.datarouter.storage.node.adapter.counter.physical;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.counter.QueueStorageWriterCounterAdapter;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.PhysicalGroupQueueStorageNode;
import io.datarouter.storage.node.op.raw.QueueStorage;
import io.datarouter.storage.node.op.raw.read.QueueStorageReader;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class PhysicalGroupQueueStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalGroupQueueStorageNode<PK,D,F>>
extends QueueStorageWriterCounterAdapter<PK,D,F,N>
implements PhysicalGroupQueueStorageNode<PK,D,F>, PhysicalAdapterMixin<PK,D,F,N>{

	public PhysicalGroupQueueStorageCounterAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public GroupQueueMessage<PK,D> peek(Config config){
		counter.count(QueueStorageReader.OP_peek);
		GroupQueueMessage<PK,D> message = backingNode.peek(config);
		String hitOrMiss = message == null || message.isEmpty() ? "miss" : "hit";
		counter.count(QueueStorageReader.OP_peek + " " + hitOrMiss);
		return message;
	}

	@Override
	public List<GroupQueueMessage<PK,D>> peekMulti(Config config){
		counter.count(QueueStorageReader.OP_peekMulti);
		List<GroupQueueMessage<PK,D>> messages = backingNode.peekMulti(config);
		counter.count(QueueStorageReader.OP_peekMulti + " messages", messages.size());
		return messages;
	}

	@Override
	public Scanner<GroupQueueMessage<PK,D>> peekUntilEmpty(Config config){
		counter.count(QueueStorageReader.OP_peekUntilEmpty);
		return backingNode.peekUntilEmpty(config);
	}

	@Override
	public List<D> pollMulti(Config config){
		counter.count(QueueStorage.OP_pollMulti);
		List<D> databeans = backingNode.pollMulti(config);
		counter.count(QueueStorage.OP_pollMulti + " databeans", databeans.size());
		return databeans;
	}

	@Override
	public PhysicalDatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

}
