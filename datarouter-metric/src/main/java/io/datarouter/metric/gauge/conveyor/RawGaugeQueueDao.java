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
package io.datarouter.metric.gauge.conveyor;

import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.metric.dto.RawGaugeBinaryDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantBlobQueueStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RawGaugeQueueDao extends BaseDao{

	public static class RawGaugeQueueDaoParams extends BaseRedundantDaoParams{

		public RawGaugeQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final BlobQueueStorageNode<RawGaugeBinaryDto> node;

	@Inject
	public RawGaugeQueueDao(
			Datarouter datarouter,
			RawGaugeQueueDaoParams params,
			QueueNodeFactory queueNodeFactory,
			EnvironmentName environmentNameSupplier){
		super(datarouter);
		String namespace = environmentNameSupplier.get() + "-shared";
		node = Scanner.of(params.clientIds)
				.map(clientId -> queueNodeFactory
						.createBlobQueue(clientId, "RawGauge", BinaryDtoIndexedCodec.of(RawGaugeBinaryDto.class))
						.withNamespace(namespace)
						.withTag(Tag.DATAROUTER)
						.build())
				.listTo(RedundantBlobQueueStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void combineAndPut(List<RawGaugeBinaryDto> dtos){
		Scanner.of(dtos)
				.then(node::combineAndPut);
	}

	public BlobQueueConsumer<RawGaugeBinaryDto> getBlobQueueConsumer(){
		return new BlobQueueConsumer<>(node);
	}

}