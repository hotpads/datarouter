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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.QueueStorage;
import io.datarouter.storage.queue.QueueMessage;

public class PollUntilEmptyQueueStorageScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseScanner<D>{

	private final QueueStorage<PK,D> queueStorage;
	private final Scanner<QueueMessage<PK,D>> queueMessageScanner;

	public PollUntilEmptyQueueStorageScanner(QueueStorage<PK,D> queueStorage, Config config){
		this.queueStorage = queueStorage;
		this.queueMessageScanner = queueStorage.peekUntilEmpty(config);
	}

	@Override
	public boolean advance(){
		if(queueMessageScanner.advance()){
			QueueMessage<PK,D> message = queueMessageScanner.current();
			queueStorage.ack(message.getKey());
			current = message.getDatabean();
			return true;
		}
		return false;
	}

}