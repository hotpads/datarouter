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
package io.datarouter.storage.op.scan.queue;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.QueueStorageReader;
import io.datarouter.storage.queue.QueueMessage;

public class PeekMultiUntilEmptyQueueStorageScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseScanner<List<QueueMessage<PK,D>>>{

	private final QueueStorageReader<PK,D> queueStorageReader;
	private final Config config;

	public PeekMultiUntilEmptyQueueStorageScanner(QueueStorageReader<PK,D> queueStorageReader, Config config){
		this.queueStorageReader = queueStorageReader;
		this.config = config;
	}

	@Override
	public boolean advance(){
		current = queueStorageReader.peekMulti(config);
		return !current.isEmpty();
	}
}
