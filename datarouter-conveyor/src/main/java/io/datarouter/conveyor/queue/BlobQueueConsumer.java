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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.BlobQueueStorage;
import io.datarouter.storage.queue.BlobQueueMessage;

public class BlobQueueConsumer<T>{

	private final Function<Config,Optional<BlobQueueMessage<T>>> peekFunction;
	private final Consumer<BlobQueueMessage<T>> ackConsumer;

	public BlobQueueConsumer(BlobQueueStorage<T> storage){
		this.peekFunction = storage::peek;
		this.ackConsumer = storage::ack;
	}

	public Optional<BlobQueueMessage<T>> peek(Duration timeout){
		return peekFunction.apply(new Config().setTimeout(timeout));
	}

	public Optional<BlobQueueMessage<T>> peek(Duration timeout, Duration visibilityTimeout){
		return peekFunction.apply(new Config()
				.setTimeout(timeout)
				.setVisibilityTimeoutMs(visibilityTimeout.toMillis()));
	}

	public void ack(BlobQueueMessage<T> key){
		ackConsumer.accept(key);
	}

}
