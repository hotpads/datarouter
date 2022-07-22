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
package io.datarouter.storage.node.adapter.trace.physical;

import java.util.Optional;

import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.trace.BaseTraceAdapter;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.storage.queue.BlobQueueMessage.BlobQueueMessageFielder;
import io.datarouter.storage.queue.BlobQueueMessageDto;
import io.datarouter.storage.queue.BlobQueueMessageKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class PhysicalBlobQueueStorageTraceAdapter
extends BaseTraceAdapter<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder,PhysicalBlobQueueStorageNode>
implements PhysicalBlobQueueStorageNode,
		PhysicalAdapterMixin<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder,PhysicalBlobQueueStorageNode>{

	public PhysicalBlobQueueStorageTraceAdapter(PhysicalBlobQueueStorageNode backingNode){
		super(backingNode);
	}

	@Override
	public int getMaxDataSize(){
		try(var $ = startSpanForOp(OP_getMaxDataSize)){
			return backingNode.getMaxDataSize();
		}
	}

	@Override
	public void put(byte[] data, Config config){
		try(var $ = startSpanForOp(OP_put)){
			backingNode.put(data, config);
		}
	}

	@Override
	public Optional<BlobQueueMessageDto> peek(Config config){
		try(var $ = startSpanForOp(OP_peek)){
			Optional<BlobQueueMessageDto> message = backingNode.peek(config);
			TracerTool.appendToSpanInfo(message.isPresent() ? "hit" : "miss");
			return message;
		}
	}

	@Override
	public void ack(byte[] handle, Config config){
		try(var $ = startSpanForOp(OP_ack)){
			backingNode.ack(handle, config);
		}
	}

	@Override
	public Optional<BlobQueueMessageDto> poll(Config config){
		try(var $ = startSpanForOp(OP_poll)){
			Optional<BlobQueueMessageDto> message = backingNode.poll(config);
			TracerTool.appendToSpanInfo(message.isPresent() ? "hit" : "miss");
			return message;
		}
	}

	@Override
	public PhysicalDatabeanFieldInfo<BlobQueueMessageKey, BlobQueueMessage, BlobQueueMessageFielder> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

}
