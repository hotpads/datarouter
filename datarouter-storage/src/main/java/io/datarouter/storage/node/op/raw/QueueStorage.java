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
package io.datarouter.storage.node.op.raw;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.QueueStorageReader;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;

/**
 * A wrapper class including a variety of poll() methods. Poll lets you consume a message from a queue in one call,
 * rather than having to peek() and ack().  Of course, it can be dangerous to ack the message before successfully
 * processing it, but sometimes that is ok.
 */
public interface QueueStorage<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends QueueStorageWriter<PK,D>, QueueStorageReader<PK,D>{

	public static final String OP_poll = "poll";
	public static final String OP_pollMulti = "pollMulti";
	public static final String OP_pollUntilEmpty = "pollUntilEmpty";


	D poll(Config config);

	default D poll(){
		return poll(new Config());
	}

	List<D> pollMulti(Config config);

	default List<D> pollMulti(){
		return pollMulti(new Config());
	}

	Scanner<D> pollUntilEmpty(Config config);

	default Scanner<D> pollUntilEmpty(){
		return pollUntilEmpty(new Config());
	}

	/*---------------------------- sub-interfaces ---------------------------*/

	public interface QueueStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends QueueStorage<PK,D>, QueueStorageWriterNode<PK,D,F>{
	}

	public interface PhysicalQueueStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends QueueStorageNode<PK,D,F>, PhysicalQueueStorageWriterNode<PK,D,F>{
	}

}
