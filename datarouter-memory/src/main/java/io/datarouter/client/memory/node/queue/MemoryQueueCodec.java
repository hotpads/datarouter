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
package io.datarouter.client.memory.node.queue;

import java.util.Map;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.memory.util.MemoryDatabeanCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.types.Ulid;

public class MemoryQueueCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final MemoryDatabeanCodec<PK,D,F> databeanCodec;

	public MemoryQueueCodec(DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.databeanCodec = new MemoryDatabeanCodec<>(fieldInfo);
	}

	public MemoryQueueMessage databeanToMemoryMessage(D databean){
		return new MemoryQueueMessage(new Ulid().value(), databeanToBytes(databean));
	}

	public QueueMessage<PK,D> memoryMessageToQueueMessage(MemoryQueueMessage memoryMessage){
		byte[] handle = idToBytes(memoryMessage.getId());
		D databean = bytesToDatabean(memoryMessage.getValue());
		return new QueueMessage<>(handle, databean, Map.of());
	}

	public static byte[] idToBytes(String id){
		return StringCodec.UTF_8.encode(id);
	}

	public static String bytesToId(byte[] bytes){
		return StringCodec.UTF_8.decode(bytes);
	}

	private byte[] databeanToBytes(D databean){
		return databeanCodec.encode(databean);
	}

	private D bytesToDatabean(byte[] bytes){
		return databeanCodec.decode(bytes);
	}

}
