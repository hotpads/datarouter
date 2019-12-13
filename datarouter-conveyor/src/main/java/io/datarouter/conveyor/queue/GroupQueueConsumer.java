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
package io.datarouter.conveyor.queue;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;

public class GroupQueueConsumer<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	private final Function<Config,GroupQueueMessage<PK,D>> peekFunction;
	private final Consumer<QueueMessageKey> ackConsumer;

	public GroupQueueConsumer(Function<Config,GroupQueueMessage<PK,D>> peekFunction,
			Consumer<QueueMessageKey> ackConsumer){
		this.peekFunction = peekFunction;
		this.ackConsumer = ackConsumer;
	}

	public GroupQueueMessage<PK,D> peek(Duration timeout){
		return peekFunction.apply(new Config().setTimeout(timeout));
	}

	public void ack(QueueMessageKey key){
		ackConsumer.accept(key);
	}

}
