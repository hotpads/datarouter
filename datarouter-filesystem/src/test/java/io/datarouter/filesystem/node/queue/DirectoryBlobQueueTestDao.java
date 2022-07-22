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
package io.datarouter.filesystem.node.queue;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.filesystem.client.FilesystemTestClientIds;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage;
import io.datarouter.storage.queue.BlobQueueMessageDto;

@Singleton
public class DirectoryBlobQueueTestDao extends BaseDao implements TestDao{

	private final BlobQueueStorage node;

	@Inject
	public DirectoryBlobQueueTestDao(Datarouter context, QueueNodeFactory queueNodeFactory){
		super(context);
		node = queueNodeFactory.createBlobQueue(FilesystemTestClientIds.TEST, "DirectoryBlobQueueTest")
				.buildAndRegister();
	}

	public void put(byte[] data){
		node.put(data);
	}

	public Optional<BlobQueueMessageDto> peek(){
		return node.peek();
	}

	public void ack(BlobQueueMessageDto key){
		node.ack(key);
	}

	public Optional<BlobQueueMessageDto> poll(){
		return node.poll();
	}

	public int getMaxDataSize(){
		return node.getMaxDataSize();
	}

}
