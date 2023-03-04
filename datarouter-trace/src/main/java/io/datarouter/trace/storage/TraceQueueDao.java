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
package io.datarouter.trace.storage;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;
import io.datarouter.storage.tag.Tag;
import io.datarouter.trace.storage.binarydto.TraceBinaryDto;
import io.datarouter.virtualnode.redundant.RedundantBlobQueueStorageNode;

@Singleton
public class TraceQueueDao extends BaseDao{

	public static class TraceQueueDaoParams extends BaseRedundantDaoParams{

		public TraceQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final BlobQueueStorageNode<TraceBinaryDto> node;

	@Inject
	public TraceQueueDao(Datarouter datarouter, TraceQueueDaoParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					var node = queueNodeFactory
							.createBlobQueue(clientId, "TraceBinaryDto", BinaryDtoIndexedCodec.of(TraceBinaryDto.class))
							.withNamespace("shared")
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantBlobQueueStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void combineAndPut(Scanner<TraceBinaryDto> dtos){
		dtos.then(node::combineAndPut);
	}

	public BlobQueueConsumer<TraceBinaryDto> getBlobQueueConsumer(){
		return new BlobQueueConsumer<>(node);
	}

}
