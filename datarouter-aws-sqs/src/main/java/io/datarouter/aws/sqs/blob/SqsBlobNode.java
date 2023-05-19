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
package io.datarouter.aws.sqs.blob;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsBlobOpFactory;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsPhysicalNode;
import io.datarouter.aws.sqs.SqsQueueNameService;
import io.datarouter.aws.sqs.service.QueueUrlAndName;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.bytestringcodec.Base64ByteStringCodec;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.util.singletonsupplier.SingletonSupplier;

public class SqsBlobNode<T>
extends BasePhysicalNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder>
implements PhysicalBlobQueueStorageNode<T>, SqsPhysicalNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder>{
	private static final Logger logger = LoggerFactory.getLogger(SqsBlobNode.class);

	private final String queueName;
	private final NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> params;
	private final Codec<T,byte[]> codec;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;
	private final boolean owned;
	private final Supplier<QueueUrlAndName> queueUrlAndName;
	private final SqsBlobOpFactory opFactory;

	public SqsBlobNode(
			SqsQueueNameService sqsQueueNameService,
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> params,
			Codec<T,byte[]> codec,
			ClientType<?,?> clientType,
			SqsClientManager sqsClientManager){
		super(params, clientType);
		this.queueName = sqsQueueNameService.buildQueueName(params.getQueueUrl(), getFieldInfo().getTableName());
		this.params = params;
		this.codec = codec;
		this.sqsClientManager = sqsClientManager;
		this.clientId = params.getClientId();
		this.owned = params.getQueueUrl() == null;
		this.queueUrlAndName = SingletonSupplier.of(this::getOrCreateQueueUrl);
		this.opFactory = new SqsBlobOpFactory(this, sqsClientManager, clientId);
	}

	private QueueUrlAndName getOrCreateQueueUrl(){
		String queueUrl;
		if(owned){
			queueUrl = createQueueAndGetUrl(queueName);
			sqsClientManager.updateAttr(clientId, queueUrl, QueueAttributeName.MessageRetentionPeriod,
					BaseSqsNode.RETENTION_S);
			logger.warn("retention updated queueName=" + queueName);
		}else{
			queueUrl = params.getQueueUrl();
		}
		logger.warn("nodeName={}, queueUrl={}", getName(), queueUrl);
		return new QueueUrlAndName(queueUrl, queueName);
	}

	private String createQueueAndGetUrl(String queueName){
		var createQueueRequest = new CreateQueueRequest(queueName);
		try{
			return sqsClientManager.getAmazonSqs(clientId).createQueue(createQueueRequest).getQueueUrl();
		}catch(RuntimeException e){
			throw new RuntimeException("queueName=" + queueName + " queueNameLength=" + queueName.length(), e);
		}
	}

	@Override
	public Supplier<QueueUrlAndName> getQueueUrlAndName(){
		return queueUrlAndName;
	}

	@Override
	public boolean getAgeMonitoringStatusForMetricAlert(){
		return params.getAgeMonitoringStatus();
	}

	@Override
	public Duration getCustomMessageAgeThreshold(){
		return params.getCustomMessageAgeThreshold();
	}

	@Override
	public int getMaxRawDataSize(){
		//SQS does not allow raw bytes, so the data must be Baes64 encoded
		return Base64ByteStringCodec.getMaxByteLength(CommonFieldSizes.MAX_SQS_SIZE);
	}

	@Override
	public Codec<T,byte[]> getCodec(){
		return codec;
	}

	@Override
	public void putRaw(byte[] data, Config config){
		opFactory.makePutOp(data, config).call();
	}

	@Override
	public Optional<BlobQueueMessage<T>> peek(Config config){
		return Optional.ofNullable(opFactory.makePeekOp(config).call())
				.map(rawDto -> new BlobQueueMessage<>(rawDto, codec));
	}

	@Override
	public void ack(byte[] handle, Config config){
		opFactory.makeAckOp(handle, config).call();
	}

}
