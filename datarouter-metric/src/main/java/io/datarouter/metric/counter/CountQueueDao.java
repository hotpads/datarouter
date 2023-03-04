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
package io.datarouter.metric.counter;

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
import io.datarouter.virtualnode.redundant.RedundantBlobQueueStorageNode;

@Singleton
public class CountQueueDao extends BaseDao{

	public static class DatarouterCountQueueDaoParams extends BaseRedundantDaoParams{

		public DatarouterCountQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	@Deprecated
	private final BlobQueueStorageNode<CountBinaryDto> node;
	// use V2 when SingleCountBinaryDto.count, min and max have values
	private final BlobQueueStorageNode<CountBinaryDto> nodeV2;

	@Inject
	public CountQueueDao(
			Datarouter datarouter,
			DatarouterCountQueueDaoParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					var node = queueNodeFactory
							.createBlobQueue(clientId,
									"CountBinaryDto",
									BinaryDtoIndexedCodec.of(CountBinaryDto.class))
							.withNamespace("shared")
							.withTag(Tag.DATAROUTER)
							.withAgeMonitoring(false)
							.build();
					return node;
				})
				.listTo(RedundantBlobQueueStorageNode::makeIfMulti);
		nodeV2 = Scanner.of(params.clientIds)
				.map(clientId -> {
					var node = queueNodeFactory
							.createBlobQueue(clientId,
									"CountBinaryDtoV2",
									BinaryDtoIndexedCodec.of(CountBinaryDto.class))
							.withNamespace("shared")
							.withTag(Tag.DATAROUTER)
							.withAgeMonitoring(false)
							.build();
					return node;
				})
				.listTo(RedundantBlobQueueStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	@Deprecated
	public void combineAndPut(List<CountBinaryDto> dtos){
		Scanner.of(dtos)
				.then(node::combineAndPut);
	}

	// use V2 when SingleCountBinaryDto.count, min and max have values
	public void combineAndPutV2(List<CountBinaryDto> dtos){
		Scanner.of(dtos)
				.then(nodeV2::combineAndPut);
	}

	@Deprecated
	public BlobQueueConsumer<CountBinaryDto> getBlobQueueConsumer(){
		return new BlobQueueConsumer<>(node);
	}

	public BlobQueueConsumer<CountBinaryDto> getBlobQueueConsumerV2(){
		return new BlobQueueConsumer<>(nodeV2);
	}

}
