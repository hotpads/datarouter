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
package io.datarouter.client.mysql;

import java.util.List;
import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.node.MysqlNode;
import io.datarouter.client.mysql.node.MysqlNodeManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.BaseClientNodeFactory;
import io.datarouter.storage.client.imp.WrappedNodeFactory;
import io.datarouter.storage.client.imp.WrappedSubEntityNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.adapter.availability.PhysicalIndexedSortedMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalIndexedSortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.counter.physical.PhysicalIndexedSortedMapStorageCounterAdapter;
import io.datarouter.storage.node.adapter.sanitization.physical.PhysicalIndexedSortedMapStorageSanitizationAdapter;
import io.datarouter.storage.node.adapter.trace.physical.PhysicalIndexedSortedMapStorageTraceAdapter;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;

@Singleton
public class MysqlClientNodeFactory extends BaseClientNodeFactory{

	private final PhysicalIndexedSortedMapStorageAvailabilityAdapterFactory
			physicalIndexedSortedMapStorageAvailabilityAdapterFactory;
	private final MysqlClientType mysqlClientType;
	private final MysqlNodeManager mysqlNodeManager;

	@Inject
	public MysqlClientNodeFactory(
			PhysicalIndexedSortedMapStorageAvailabilityAdapterFactory availabilityAdapterFactory,
			MysqlClientType mysqlClientType,
			MysqlNodeManager mysqlNodeManager){
		this.physicalIndexedSortedMapStorageAvailabilityAdapterFactory = availabilityAdapterFactory;
		this.mysqlClientType = mysqlClientType;
		this.mysqlNodeManager = mysqlNodeManager;
	}

	public class MysqlWrappedNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedNodeFactory<EK,E,PK,D,F,PhysicalIndexedSortedMapStorageNode<PK,D,F>>{

		@Override
		public PhysicalIndexedSortedMapStorageNode<PK,D,F> createNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return new MysqlNode<>(nodeParams, mysqlClientType, mysqlNodeManager);
		}

		@Override
		public List<UnaryOperator<PhysicalIndexedSortedMapStorageNode<PK,D,F>>> getAdapters(){
			return List.of(
					PhysicalIndexedSortedMapStorageSanitizationAdapter::new,
					PhysicalIndexedSortedMapStorageCounterAdapter::new,
					PhysicalIndexedSortedMapStorageTraceAdapter::new,
					physicalIndexedSortedMapStorageAvailabilityAdapterFactory::create,
					PhysicalIndexedSortedMapStorageCallsiteAdapter::new);
		}

	}

	public class MysqlWrappedSubEntityNodeFactory<
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends WrappedSubEntityNodeFactory<EK,E,PK,D,F,PhysicalIndexedSortedMapStorageNode<PK,D,F>>{

		private final MysqlWrappedNodeFactory<EK,E,PK,D,F> factory = new MysqlWrappedNodeFactory<>();

		@Override
		public PhysicalIndexedSortedMapStorageNode<PK,D,F> createSubEntityNode(
				EntityNodeParams<EK,E> entityNodeParams,
				NodeParams<PK,D,F> nodeParams){
			return factory.createNode(entityNodeParams, nodeParams);
		}

		@Override
		public List<UnaryOperator<PhysicalIndexedSortedMapStorageNode<PK,D,F>>> getAdapters(){
			return factory.getAdapters();
		}

	}

	@Override
	protected <
			EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedNodeFactory<EK,E,PK,D,F,?> makeWrappedNodeFactory(){
		return new MysqlWrappedNodeFactory<>();
	}

	@Override
	protected <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	WrappedSubEntityNodeFactory<EK,E,PK,D,F,?> makeWrappedSubEntityNodeFactory(){
		return new MysqlWrappedSubEntityNodeFactory<>();
	}

}
