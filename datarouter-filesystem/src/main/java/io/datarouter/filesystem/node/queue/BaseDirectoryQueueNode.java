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
package io.datarouter.filesystem.node.queue;

import java.util.Collection;

import io.datarouter.filesystem.raw.queue.DirectoryQueue;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.QueueMessageKey;

public abstract class BaseDirectoryQueueNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements QueueStorageWriter<PK,D>{

	protected final DirectoryQueue directoryQueue;

	public BaseDirectoryQueueNode(DirectoryQueue directoryQueue, NodeParams<PK,D,F> params){
		super(params, null);
		this.directoryQueue = directoryQueue;
	}

	@Override
	public void ack(QueueMessageKey key, Config config){
		directoryQueue.ack(key.getStringHandle());
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		keys.forEach(key -> ack(key, config));
	}

}
