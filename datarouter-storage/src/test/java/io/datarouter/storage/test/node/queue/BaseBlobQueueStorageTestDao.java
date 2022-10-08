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
package io.datarouter.storage.test.node.queue;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.bytes.Codec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.BlobQueueMessage;

public class BaseBlobQueueStorageTestDao<T> extends BaseDao implements TestDao{

	protected final BlobQueueStorageNode<T> node;

	@Inject
	public BaseBlobQueueStorageTestDao(
			Datarouter datarouter,
			QueueNodeFactory queueNodeFactory,
			ClientId clientId,
			String nodeName,
			Codec<T,byte[]> codec){
		super(datarouter);
		node = queueNodeFactory.createBlobQueue(
				clientId,
				nodeName,
				codec)
				.buildAndRegister();
	}

	public void putRaw(byte[] data){
		node.putRaw(data);
	}

	public Optional<BlobQueueMessage<T>> peek(){
		return node.peek();
	}

	public void ack(BlobQueueMessage<T> key){
		node.ack(key);
	}

	public Optional<BlobQueueMessage<T>> poll(){
		return node.poll();
	}

	public int getMaxDataSize(){
		return node.getMaxRawDataSize();
	}

	public void combineAndPut(List<T> data){
		Scanner.of(data)
				.then(node::combineAndPut);
	}

}
