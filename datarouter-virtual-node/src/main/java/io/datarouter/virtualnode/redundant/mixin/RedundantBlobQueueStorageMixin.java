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
package io.datarouter.virtualnode.redundant.mixin;

import java.util.Optional;

import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.BlobQueueStorage;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.storage.queue.BlobQueueMessage.BlobQueueMessageFielder;
import io.datarouter.storage.queue.BlobQueueMessageDto;
import io.datarouter.storage.queue.BlobQueueMessageKey;
import io.datarouter.virtualnode.redundant.RedundantQueueNode;

public interface RedundantBlobQueueStorageMixin
extends BlobQueueStorage,
RedundantQueueNode<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder,BlobQueueStorageNode>{

	@Override
	default int getMaxDataSize(){
		return getWriteNode().getMaxDataSize();
	}

	@Override
	default void put(byte[] data, Config config){
		getWriteNode().put(data, config);
	}

	@Override
	default Optional<BlobQueueMessageDto> peek(Config config){
		return Scanner.of(getReadNodes())
				.map(BlobQueueStorageNode::peek)
				.concat(OptionalScanner::of)
				.findFirst();
	}

	@Override
	default void ack(byte[] handle, Config config){
		getWriteNode().ack(handle, config);
	}

	@Override
	default Optional<BlobQueueMessageDto> poll(){
		return Scanner.of(getReadNodes())
				.map(BlobQueueStorageNode::poll)
				.concat(OptionalScanner::of)
				.findFirst();
	}

}
