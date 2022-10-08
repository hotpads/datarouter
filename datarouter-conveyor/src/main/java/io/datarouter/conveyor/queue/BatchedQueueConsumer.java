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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class BatchedQueueConsumer<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	private final Function<Config,List<QueueMessage<PK,D>>> peekMultiFunction;
	private final BiConsumer<Collection<QueueMessageKey>,Config> ackMultiConsumer;

	public BatchedQueueConsumer(
			Function<Config,List<QueueMessage<PK,D>>> peekMultiFunction,
			BiConsumer<Collection<QueueMessageKey>,Config> ackMultiConsumer){
		this.peekMultiFunction = peekMultiFunction;
		this.ackMultiConsumer = ackMultiConsumer;
	}

	public List<QueueMessage<PK,D>> peekMulti(Integer limit, Duration timeout, Duration visibilityTimeout){
		return peekMultiFunction.apply(new Config()
				.setLimit(limit)
				.setTimeout(timeout)
				.setVisibilityTimeoutMs(visibilityTimeout.toMillis()));
	}

	public void ackMulti(Integer limit, Collection<QueueMessageKey> keys){
		ackMultiConsumer.accept(keys, new Config().setLimit(limit));
	}

}
