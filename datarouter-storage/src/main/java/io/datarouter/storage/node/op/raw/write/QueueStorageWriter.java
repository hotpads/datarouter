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
package io.datarouter.storage.node.op.raw.write;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.queue.QueueMessageKey;

/**
 * Methods to acknowledge processing of a queue message so that the messaging service can safely delete the message.
 */
public interface QueueStorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends StorageWriter<PK,D>{

	String OP_ack = "ack";
	String OP_ackMulti = "ackMulti";

	void ack(QueueMessageKey key, Config config);

	default void ack(QueueMessageKey key){
		ack(key, new Config());
	}

	void ackMulti(Collection<QueueMessageKey> keys, Config config);

	default void ackMulti(Collection<QueueMessageKey> keys){
		ackMulti(keys, new Config());
	}

	/*---------------------------- sub-interfaces ---------------------------*/

	interface QueueStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends QueueStorageWriter<PK,D>, StorageWriterNode<PK,D,F>{
	}

	interface PhysicalQueueStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends QueueStorageWriterNode<PK,D,F>, PhysicalNode<PK,D,F>{
	}

}
