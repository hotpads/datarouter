/**
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.CreateQueueRequest;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.util.lazy.Lazy;
import io.datarouter.util.string.StringTool;

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
	// SQS default is 30 sec
	public static final long DEFAULT_VISIBILITY_TIMEOUT_MS = Duration.ofSeconds(30).toMillis();

	private final DatarouterProperties datarouterProperties;
	private final DatarouterService datarouterService;
	private final NodeParams<PK,D,F> params;
	private final Lazy<String> queueUrl;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;
	protected final SqsOpFactory<PK,D,F> sqsOpFactory;

	public BaseSqsNode(
			DatarouterProperties datarouterProperties,
			DatarouterService datarouterService,
			NodeParams<PK,D,F> params,
			SqsClientType sqsClientType,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(params, sqsClientType);
		this.datarouterProperties = datarouterProperties;
		this.datarouterService = datarouterService;
		this.params = params;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
		this.queueUrl = Lazy.of(this::getOrCreateQueueUrl);
		this.sqsOpFactory = new SqsOpFactory<>(this, sqsClientManager, clientId);
	}

	private String getOrCreateQueueUrl(){
		String queueUrl;
		if(params.getQueueUrl() != null){
			queueUrl = params.getQueueUrl();
			//don't issue the createQueue request because it is probably someone else's queue
		}else{
			String serviceName = datarouterService.getName();
			String namespace = params.getNamespace().orElse(datarouterProperties.getEnvironment() + "-" + serviceName);
			String queueName = StringTool.isEmpty(namespace)
					? getFieldInfo().getTableName()
					: namespace + "-" + getFieldInfo().getTableName();
			queueUrl = tryCreateQueueAndGetUrl(queueName);
		}
		logger.warn("nodeName={}, queueName={}", getName(), queueUrl);
		return queueUrl;
	}

	private String tryCreateQueueAndGetUrl(String queueName){
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
		try{
			return sqsClientManager.getAmazonSqs(clientId).createQueue(createQueueRequest).getQueueUrl();
		}catch(RuntimeException e){
			throw new RuntimeException("queueName=" + queueName + " queueNameLength=" + queueName.length(), e);
		}
	}

	public Lazy<String> getQueueUrl(){
		return queueUrl;
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
