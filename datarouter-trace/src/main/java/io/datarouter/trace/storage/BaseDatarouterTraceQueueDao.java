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
package io.datarouter.trace.storage;

import java.util.Collection;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.GroupQueueConsumer;

public interface BaseDatarouterTraceQueueDao{

	GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> getGroupQueueConsumer();
	void putMulti(Collection<ConveyorMessage> databeans);

	static class NoOpDatarouterTraceQueueDao implements BaseDatarouterTraceQueueDao{

		@Override
		public GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> getGroupQueueConsumer(){
			return new GroupQueueConsumer<>($ -> null, null);
		}

		@Override
		public void putMulti(Collection<ConveyorMessage> databeans){
		}

	}

}
