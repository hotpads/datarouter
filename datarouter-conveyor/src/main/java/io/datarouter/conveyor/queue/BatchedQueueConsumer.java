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
package io.datarouter.conveyor.queue;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class BatchedQueueConsumer<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	private final Function<Config,QueueMessage<PK,D>> peekFunction;
	private final Consumer<Collection<QueueMessageKey>> ackMultiConsumer;

	public BatchedQueueConsumer(
			Function<Config,QueueMessage<PK,D>> peekFunction,
			Consumer<Collection<QueueMessageKey>> ackMultiConsumer){
		this.peekFunction = peekFunction;
		this.ackMultiConsumer = ackMultiConsumer;
	}

	public QueueMessage<PK,D> peek(Duration timeout, Duration visibilityTimeout){
		return peekFunction.apply(new Config()
				.setTimeout(timeout)
				.setVisibilityTimeoutMs(visibilityTimeout.toMillis()));
	}

	public void ackMulti(Collection<QueueMessageKey> keys){
		ackMultiConsumer.accept(keys);
	}

}
