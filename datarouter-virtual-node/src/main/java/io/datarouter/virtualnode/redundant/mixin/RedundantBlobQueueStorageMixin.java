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

import io.datarouter.bytes.Codec;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.BlobQueueStorage;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.virtualnode.redundant.RedundantQueueNode;

public interface RedundantBlobQueueStorageMixin<T>
extends BlobQueueStorage<T>,
		RedundantQueueNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder,BlobQueueStorageNode<T>>{

	@Override
	default int getMaxRawDataSize(){
		return getWriteNode().getMaxRawDataSize();
	}

	@Override
	default Codec<T,byte[]> getCodec(){
		return getWriteNode().getCodec();
	}

	@Override
	default void putRaw(byte[] data, Config config){
		getWriteNode().putRaw(data, config);
	}

	@Override
	default Optional<BlobQueueMessage<T>> peek(Config config){
		return Scanner.of(getReadNodes())
				.concatOpt(BlobQueueStorageNode::peek)
				.findFirst();
	}

	@Override
	default void ack(byte[] handle, Config config){
		getWriteNode().ack(handle, config);
	}

	@Override
	default Optional<BlobQueueMessage<T>> poll(Config config){
		return Scanner.of(getReadNodes())
				.concatOpt(BlobQueueStorageNode::poll)
				.findFirst();
	}

}
