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
package io.datarouter.aws.sqs;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.aws.sqs.service.QueueUrlAndName;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.util.singletonsupplier.SingletonSupplier;

public abstract class BaseSqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements QueueStorageWriter<PK,D>, SqsPhysicalNode<PK,D,F>{
	private static final Logger logger = LoggerFactory.getLogger(BaseSqsNode.class);

	// do not change, this is a limit from SQS
	public static final int MAX_MESSAGES_PER_BATCH = 10;
	public static final Duration MAX_TIMEOUT = Duration.ofSeconds(20);

	// SQS default is 30 sec
	public static final long DEFAULT_VISIBILITY_TIMEOUT_MS = Duration.ofSeconds(30).toMillis();
	public static final long RETENTION_S = Duration.ofDays(14).getSeconds();

	private final String queueName;
	private final NodeParams<PK,D,F> params;
	private final Supplier<QueueUrlAndName> queueUrlAndName;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;
	protected final SqsOpFactory<PK,D,F> sqsOpFactory;
	private final boolean owned;

	public BaseSqsNode(
			SqsQueueNameService sqsQueueNameService,
			NodeParams<PK,D,F> params,
			SqsClientType sqsClientType,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(params, sqsClientType);
		this.queueName = sqsQueueNameService.buildQueueName(params.getQueueUrl(), getFieldInfo().getTableName());
		this.params = params;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
		this.queueUrlAndName = SingletonSupplier.of(this::getOrCreateQueueUrl);
		this.sqsOpFactory = new SqsOpFactory<>(this, sqsClientManager, clientId);
		this.owned = params.getQueueUrl() == null;
	}

	private QueueUrlAndName getOrCreateQueueUrl(){
		String queueUrl;
		if(owned){
			queueUrl = createQueueAndGetUrl(queueName);
			sqsClientManager.updateAttr(clientId, queueUrl, QueueAttributeName.MessageRetentionPeriod, RETENTION_S);
			logger.warn("retention updated queueName={}", queueName);
		}else{
			queueUrl = params.getQueueUrl();
		}
		logger.warn("nodeName={}, queueUrl={}", getName(), queueUrl);
		return new QueueUrlAndName(queueUrl, queueName);
	}

	private String createQueueAndGetUrl(String queueName){
		var createQueueRequest = new CreateQueueRequest(queueName);
//				.addAttributesEntry(QueueAttributeName.MessageRetentionPeriod.name(), String.valueOf(RETENTION_S));
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

	public boolean isOwned(){
		return owned;
	}

	@Override
	public void ack(QueueMessageKey key, Config config){
		sqsOpFactory.makeAckOp(key, config).call();
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		sqsOpFactory.makeAckMultiOp(keys, config).call();
	}

}
