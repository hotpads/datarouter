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

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantBlobQueueStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CountQueueDao extends BaseDao{

	public record DatarouterCountQueueDaoParams(List<ClientId> clientIds){
	}

	private final BlobQueueStorageNode<CountBinaryDto> nodeV2;

	@Inject
	public CountQueueDao(
			Datarouter datarouter,
			DatarouterCountQueueDaoParams params,
			QueueNodeFactory queueNodeFactory,
			EnvironmentName environmentNameSupplier){
		super(datarouter);
		@SuppressWarnings("deprecation")
		String namespace = environmentNameSupplier.deprecatedIsProduction()
				? "shared"
				: environmentNameSupplier.get() + "-shared";
		nodeV2 = Scanner.of(params.clientIds)
				.map(clientId -> queueNodeFactory
						.createBlobQueue(
								clientId,
								"CountBinaryDto",
								BinaryDtoIndexedCodec.of(CountBinaryDto.class))
						.withNamespace(namespace)
						.withTag(Tag.DATAROUTER)
						.withAgeMonitoring(false)
						.build())
				.listTo(RedundantBlobQueueStorageNode::makeIfMulti);
		datarouter.register(nodeV2);
	}

	public void combineAndPut(List<CountBinaryDto> dtos){
		Scanner.of(dtos)
				.then(nodeV2::combineAndPut);
	}

	public BlobQueueConsumer<CountBinaryDto> getBlobQueueConsumer(){
		return new BlobQueueConsumer<>(nodeV2);
	}

}
