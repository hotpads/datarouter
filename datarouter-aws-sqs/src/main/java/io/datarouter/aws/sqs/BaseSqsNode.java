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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Twin;
import io.datarouter.web.config.service.ServiceName;

public abstract class BaseSqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements QueueStorageWriter<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(BaseSqsNode.class);

	// do not change, this is a limit from SQS
	public static final int MAX_MESSAGES_PER_BATCH = 10;
	public static final int MAX_TIMEOUT_SECONDS = 20;
	public static final int MAX_BYTES_PER_MESSAGE = 256 * 1024;
	public static final int MAX_BYTES_PER_PAYLOAD = 256 * 1024;
	public static final int MIN_QUEUE_NAME_LENGTH = 1;
	public static final int MAX_QUEUE_NAME_LENGTH = 80;

	// SQS default is 30 sec
	public static final long DEFAULT_VISIBILITY_TIMEOUT_MS = Duration.ofSeconds(30).toMillis();
	private static final long RETENTION_S = Duration.ofDays(14).getSeconds();

	private final EnvironmentName environmentName;
	private final ServiceName serviceName;
	private final NodeParams<PK,D,F> params;
	private final Supplier<Twin<String>> queueUrlAndName;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;
	protected final SqsOpFactory<PK,D,F> sqsOpFactory;
	private final boolean owned;

	public BaseSqsNode(
			EnvironmentName environmentName,
			ServiceName serviceName,
			NodeParams<PK,D,F> params,
			SqsClientType sqsClientType,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(params, sqsClientType);
		this.environmentName = environmentName;
		this.serviceName = serviceName;
		this.params = params;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
		this.queueUrlAndName = SingletonSupplier.of(this::getOrCreateQueueUrl);
		this.sqsOpFactory = new SqsOpFactory<>(this, sqsClientManager, clientId);
		this.owned = params.getQueueUrl() == null;
	}

	private Twin<String> getOrCreateQueueUrl(){
		String queueUrl;
		String queueName;
		if(!owned){
			queueUrl = params.getQueueUrl();
			queueName = queueUrl.substring(queueUrl.lastIndexOf('/') + 1);
			//don't issue the createQueue request because it is probably someone else's queue
		}else{
			String namespace = params.getNamespace()
					.orElse(buildFullNameSpace(environmentName.get(), serviceName.get()));
			queueName = StringTool.isEmpty(namespace)
					? getFieldInfo().getTableName()
					: buildFullQueueName(environmentName.get(), serviceName.get(), getFieldInfo().getTableName());
			if(queueName.length() > MAX_QUEUE_NAME_LENGTH){
				// Future change to a throw.
				logger.error("queue={} overflows the max size {}", queueName, MAX_QUEUE_NAME_LENGTH);
			}
			queueUrl = createQueueAndGetUrl(queueName);
			sqsClientManager.updateAttr(clientId, queueUrl, QueueAttributeName.MessageRetentionPeriod, RETENTION_S);
			logger.warn("retention updated queueName=" + queueName);
		}
		logger.warn("nodeName={}, queueUrl={}", getName(), queueUrl);
		return new Twin<>(queueUrl, queueName);
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

	private static String buildFullNameSpace(String environment, String serviceName){
		return environment + "-" + serviceName;
	}

	public static String buildFullQueueName(String environment, String serviceName, String tableName){
		return buildFullNameSpace(environment, serviceName) + "-" + tableName;
	}

	public Supplier<Twin<String>> getQueueUrlAndName(){
		return queueUrlAndName;
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
