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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.sqs.blob.SqsBlobNode;
import io.datarouter.aws.sqs.group.SqsGroupNode;
import io.datarouter.aws.sqs.single.SqsNode;
import io.datarouter.bytes.Codec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;

@Singleton
public class SqsNodeFactory{

	@Inject
	private EnvironmentName environmentName;
	@Inject
	private ServiceName serviceName;
	@Inject
	private SqsClientType sqsClientType;
	@Inject
	private SqsClientManager sqsClientManager;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SqsNode<PK,D,F> createSingleNode(NodeParams<PK,D,F> params){
		return new SqsNode<>(
				environmentName,
				serviceName,
				params,
				sqsClientType,
				sqsClientManager,
				params.getClientId());
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SqsGroupNode<PK,D,F> createGroupNode(NodeParams<PK,D,F> params){
		return new SqsGroupNode<>(
				environmentName,
				serviceName,
				params,
				sqsClientType,
				sqsClientManager,
				params.getClientId());
	}

	public <T> PhysicalBlobQueueStorageNode<T> createBlobQueueNode(
			NodeParams<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> params,
			Codec<T,byte[]> codec){
		return new SqsBlobNode<>(
				params,
				codec,
				sqsClientType,
				sqsClientManager,
				environmentName,
				serviceName);
	}

}
