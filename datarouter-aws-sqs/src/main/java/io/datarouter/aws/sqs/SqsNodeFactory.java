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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.sqs.group.SqsGroupNode;
import io.datarouter.aws.sqs.single.SqsNode;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.NodeParams;

@Singleton
public class SqsNodeFactory{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private SqsClientType sqsClientType;
	@Inject
	private SqsClientManager sqsClientManager;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SqsNode<PK,D,F> createSingleNode(NodeParams<PK,D,F> params){
		return new SqsNode<>(
				datarouterProperties,
				datarouterService,
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
				datarouterProperties,
				datarouterService,
				params,
				sqsClientType,
				sqsClientManager,
				params.getClientId());
	}

}
