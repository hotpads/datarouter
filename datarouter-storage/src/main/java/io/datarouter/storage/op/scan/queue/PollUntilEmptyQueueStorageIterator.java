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
package io.datarouter.storage.op.scan.queue;

import java.util.Iterator;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.QueueStorage;
import io.datarouter.storage.queue.QueueMessage;

public class PollUntilEmptyQueueStorageIterator<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
implements Iterator<D>{

	private final Iterator<QueueMessage<PK,D>> queueMessageIterator;
	private final QueueStorage<PK,D> queueStorage;

	public PollUntilEmptyQueueStorageIterator(QueueStorage<PK,D> queueStorage, Config config){
		this.queueStorage = queueStorage;
		this.queueMessageIterator = queueStorage.peekUntilEmpty(config).iterator();
	}

	@Override
	public boolean hasNext(){
		return queueMessageIterator.hasNext();
	}

	@Override
	public D next(){
		if(!hasNext()){
			return null;
		}
		QueueMessage<PK,D> message = queueMessageIterator.next();
		queueStorage.ack(message.getKey(), null);
		return message.getDatabean();
	}

	@Override
	public void remove(){
		queueMessageIterator.remove();
	}

}
