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

import java.util.Optional;

import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.storage.queue.BlobQueueMessage.BlobQueueMessageFielder;
import io.datarouter.storage.queue.BlobQueueMessageDto;
import io.datarouter.storage.queue.BlobQueueMessageKey;

public interface BlobQueueStorage{

	public static final String OP_getMaxDataSize = "getMaxDataSize";
	public static final String OP_put = "put";
	public static final String OP_peek = "peek";
	public static final String OP_ack = "ack";
	public static final String OP_poll = "poll";

	int getMaxDataSize();

	void put(byte[] data, Config config);

	default void put(byte[] data){
		put(data, new Config());
	}

	Optional<BlobQueueMessageDto> peek(Config config);

	default Optional<BlobQueueMessageDto> peek(){
		return peek(new Config());
	}

	void ack(byte[] handle, Config config);

	default void ack(byte[] handle){
		ack(handle, new Config());
	}

	default void ack(BlobQueueMessageDto blobQueueMessage, Config config){
		ack(blobQueueMessage.getHandle(), config);
	}

	default void ack(BlobQueueMessageDto blobQueueMessage){
		ack(blobQueueMessage, new Config());
	}

	default Optional<BlobQueueMessageDto> poll(Config config){
		var optionalMessage = peek(config);
		optionalMessage.ifPresent(message -> ack(message, config));
		return optionalMessage;
	}

	default Optional<BlobQueueMessageDto> poll(){
		return poll(new Config());
	}

	interface BlobQueueStorageNode
	extends BlobQueueStorage, Node<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder>{
	}

	interface PhysicalBlobQueueStorageNode
	extends BlobQueueStorageNode, PhysicalNode<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder>{
	}

}
