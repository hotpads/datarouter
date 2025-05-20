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
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;
import io.datarouter.storage.tag.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricAnnotationNonProdQueueDao extends BaseDao{

	public record MetricAnnotationNonProdQueueDaoParams(
			ClientId clientId){
	}

	private final BlobQueueStorageNode<DatarouterMetricAnnotationGroupBinaryDto> node;

	@Inject
	public MetricAnnotationNonProdQueueDao(
			Datarouter datarouter,
			MetricAnnotationNonProdQueueDaoParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		node = queueNodeFactory
				.createBlobQueue(
						params.clientId,
						"DatarouterMetricAnnotation",
						BinaryDtoIndexedCodec.of(DatarouterMetricAnnotationGroupBinaryDto.class))
				.withNamespace("nonprod-shared")
				.withTag(Tag.DATAROUTER)
				.buildAndRegister();
	}

	public void combineAndPut(List<DatarouterMetricAnnotationGroupBinaryDto> dtos){
		Scanner.of(dtos)
				.then(node::combineAndPut);
	}

	public BlobQueueConsumer<DatarouterMetricAnnotationGroupBinaryDto> getBlobQueueConsumer(){
		return new BlobQueueConsumer<>(node);
	}
}
