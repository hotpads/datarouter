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
package io.datarouter.metric.publisher;

import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;
import io.datarouter.storage.tag.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricQueueDao extends BaseDao{

	public record MetricQueueDaoParams(
			ClientId clientId){
	}

	private final BlobQueueStorageNode<DatarouterMetricGroupBinaryDto> node;

	@Inject
	public MetricQueueDao(
			Datarouter datarouter,
			MetricQueueDaoParams params,
			QueueNodeFactory queueNodeFactory,
			EnvironmentName environmentNameSupplier){
		super(datarouter);

		node = queueNodeFactory
				.createBlobQueue(
						params.clientId,
						"DatarouterMetric",
						BinaryDtoIndexedCodec.of(DatarouterMetricGroupBinaryDto.class))
				.withNamespace(environmentNameSupplier.get() + "-shared")
				.withTag(Tag.DATAROUTER)
				.buildAndRegister();
	}

	public PhysicalNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> getPhysicalNode(){
		return node.getPhysicalNodes().get(0);
	}

	public void combineAndPut(List<DatarouterMetricGroupBinaryDto> dtos){
		Scanner.of(dtos)
				.then(node::combineAndPut);
	}

	public BlobQueueConsumer<DatarouterMetricGroupBinaryDto> getBlobQueueConsumer(){
		return new BlobQueueConsumer<>(node);
	}

}
