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
package io.datarouter.storage.node.op.raw.read;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.queue.GroupQueueMessage;

public interface GroupQueueStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends NodeOps<PK,D>{

	public static final String OP_peek = "peek";
	public static final String OP_peekMulti = "peekMulti";
	public static final String OP_peekUntilEmpty = "peekUntilEmpty";

	GroupQueueMessage<PK,D> peek(Config config);

	default GroupQueueMessage<PK,D> peek(){
		return peek(new Config());
	}

	List<GroupQueueMessage<PK,D>> peekMulti(Config config);

	default List<GroupQueueMessage<PK,D>> peekMulti(){
		return peekMulti(new Config());
	}

	Scanner<GroupQueueMessage<PK,D>> peekUntilEmpty(Config config);

	default Scanner<GroupQueueMessage<PK,D>> peekUntilEmpty(){
		return peekUntilEmpty(new Config());
	}

}
