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
package io.datarouter.metric.gauge;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.binarydto.codec.bytearray.MultiBinaryDtoEncoder;
import io.datarouter.bytes.BatchingByteArrayScanner;
import io.datarouter.bytes.ByteTool;
import io.datarouter.conveyor.queue.BlobQueueConsumer;
import io.datarouter.conveyor.queue.QueueConsumer;
import io.datarouter.metric.dto.GaugeBinaryDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.node.op.raw.QueueStorage.QueueStorageNode;
import io.datarouter.storage.queue.StringQueueMessage;
import io.datarouter.storage.queue.StringQueueMessage.StringQueueMessageFielder;
import io.datarouter.storage.queue.StringQueueMessageKey;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantBlobQueueStorageNode;
import io.datarouter.virtualnode.redundant.RedundantQueueStorageNode;

@Singleton
public class GaugeBlobQueueDao extends BaseDao{

	public static class GaugeBlobQueueDaoParams extends BaseRedundantDaoParams{

		public GaugeBlobQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final QueueStorageNode<StringQueueMessageKey,StringQueueMessage,StringQueueMessageFielder> queueNode;
	private final BlobQueueStorage blobNode;

	@Inject
	public GaugeBlobQueueDao(Datarouter datarouter, GaugeBlobQueueDaoParams params, QueueNodeFactory queueNodeFactory){
		super(datarouter);
		queueNode = Scanner.of(params.clientIds)
				.map(clientId -> {
					QueueStorageNode<StringQueueMessageKey,StringQueueMessage,StringQueueMessageFielder> node
							= queueNodeFactory
							.createSingleQueue(clientId, StringQueueMessage::new, StringQueueMessageFielder::new)
							.withNamespace("shared")
							.withQueueName("GaugeBlob")
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantQueueStorageNode::makeIfMulti);

		blobNode = Scanner.of(params.clientIds)
				.map(clientId -> {
					BlobQueueStorageNode node = queueNodeFactory
							.createBlobQueue(clientId, "GaugeBinaryDto")
							.withNamespace("shared")
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantBlobQueueStorageNode::makeIfMulti);
		datarouter.register(queueNode);
	}

	public void putMulti(Collection<StringQueueMessage> databeans){
		queueNode.putMulti(databeans);
	}

	public void combineAndPut(List<GaugeBinaryDto> dtos){
		//this is not inlined because it creates a warning:
		//Resource leak: '<unassigned Closeable value>' is never closed
		var scanner = new BatchingByteArrayScanner(
				new MultiBinaryDtoEncoder<>(GaugeBinaryDto.class).encodeWithConcatenatedLength(dtos),
				blobNode.getMaxDataSize());
		scanner.map(ByteTool::concat)
				.forEach(blobNode::put);
	}

	public QueueConsumer<StringQueueMessageKey,StringQueueMessage> getQueueConsumer(){
		return new QueueConsumer<>(queueNode::peek, queueNode::ack);
	}

	public BlobQueueConsumer getBinaryDtoQueueConsumer(){
		return new BlobQueueConsumer(blobNode::peek, blobNode::ack);
	}

}