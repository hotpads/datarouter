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

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.sqs.group.SqsGroupNode;
import io.datarouter.aws.sqs.single.SqsNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BaseClientNodeFactory;
import io.datarouter.storage.client.imp.QueueClientNodeFactory;
import io.datarouter.storage.client.imp.WrappedNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalGroupQueueStorageCounterAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalQueueStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalGroupQueueStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalQueueStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalGroupQueueStorageTraceAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalQueueStorageTraceAdapter;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;

@Singleton
public class SqsClientNodeFactory extends BaseClientNodeFactory implements QueueClientNodeFactory{

	@Inject
	private SqsNodeFactory sqsNodeFactory;

	public class SqsWrappedNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedNodeFactory<EK,E,PK,D,F,PhysicalQueueStorageNode<PK,D,F>>{

		@Override
		public PhysicalQueueStorageNode<PK,D,F> createNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return sqsNodeFactory.createSingleNode(nodeParams);
		}

		@Override
		public List<UnaryOperator<PhysicalQueueStorageNode<PK,D,F>>> getAdapters(){
			return Arrays.asList(
					PhysicalQueueStorageCounterAdapter::new,
					PhysicalQueueStorageSanitizationAdapter::new,
					PhysicalQueueStorageTraceAdapter::new);
		}

	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedNodeFactory<EK,E,PK,D,F,PhysicalQueueStorageNode<PK,D,F>> makeWrappedNodeFactory(){
		return new SqsWrappedNodeFactory<>();
	}

	/*-------------- QueueClientNodeFactory --------------*/

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createSingleQueueNode(NodeParams<PK,D,F> nodeParams){
		SqsNode<PK,D,F> node = sqsNodeFactory.createSingleNode(nodeParams);
		return new PhysicalQueueStorageTraceAdapter<>(
				new PhysicalQueueStorageCounterAdapter<>(
				new PhysicalQueueStorageSanitizationAdapter<>(node)));
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D,F> createGroupQueueNode(NodeParams<PK,D,F> nodeParams){
		SqsGroupNode<PK,D,F> node = sqsNodeFactory.createGroupNode(nodeParams);
		return new PhysicalGroupQueueStorageTraceAdapter<>(
				new PhysicalGroupQueueStorageCounterAdapter<>(
				new PhysicalGroupQueueStorageSanitizationAdapter<>(node)));
	}

}
