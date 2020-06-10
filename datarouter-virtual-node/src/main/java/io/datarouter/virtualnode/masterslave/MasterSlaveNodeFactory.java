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
package io.datarouter.virtualnode.masterslave;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.node.tableconfig.NodewatchConfigurationBuilder;

@Singleton
public class MasterSlaveNodeFactory{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId masterClientId,
			Collection<ClientId> slaveClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
	N master = nodeFactory.create(masterClientId, databeanSupplier, fielderSupplier).build();
		Function<ClientId,N> buildSlaveFunction = clientId -> nodeFactory.create(clientId, databeanSupplier,
				fielderSupplier).build();
		List<N> slaves = Scanner.of(slaveClientIds).map(buildSlaveFunction).list();
		return make(master, slaves);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId masterClientId,
			Collection<ClientId> slaveClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			String tableName){
		N master = nodeFactory.create(masterClientId, databeanSupplier, fielderSupplier)
				.withTableName(tableName)
				.build();
		Function<ClientId,N> buildSlaveFunction = clientId -> nodeFactory.create(clientId, databeanSupplier,
				fielderSupplier).withTableName(tableName).build();
		List<N> slaves = Scanner.of(slaveClientIds).map(buildSlaveFunction).list();
		return make(master, slaves);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId masterClientId,
			Collection<ClientId> slaveClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			NodewatchConfigurationBuilder nodewatchConfigurationBuilder){
		N master = nodeFactory.create(masterClientId, databeanSupplier, fielderSupplier)
				.withNodewatchConfigurationBuilder(nodewatchConfigurationBuilder)
				.build();
		Function<ClientId,N> buildSlaveFunction = clientId -> nodeFactory.create(clientId, databeanSupplier,
				fielderSupplier).build();
		List<N> slaves = Scanner.of(slaveClientIds).map(buildSlaveFunction).list();
		return make(master, slaves);
	}

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId masterClientId,
			Collection<ClientId> slaveClientIds,
			Supplier<EK> entityKeySupplier,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		N master = nodeFactory.create(masterClientId, entityKeySupplier, databeanSupplier, fielderSupplier).build();
		Function<ClientId,N> buildSlaveFunction = clientId -> nodeFactory.create(clientId, entityKeySupplier,
				databeanSupplier, fielderSupplier).build();
		List<N> slaves = Scanner.of(slaveClientIds).map(buildSlaveFunction).list();
		return make(master, slaves);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N register(
			ClientId masterClientId,
			Collection<ClientId> slaveClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return datarouter.register(build(masterClientId, slaveClientIds, databeanSupplier, fielderSupplier));
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N register(
			ClientId masterClientId,
			Collection<ClientId> slaveClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			String tableName){
		return datarouter.register(build(masterClientId, slaveClientIds, databeanSupplier, fielderSupplier, tableName));
	}

	@SuppressWarnings("unchecked")
	public static <
			PK extends EntityPrimaryKey<?,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N make(N master, List<N> slaves){
		if(master instanceof IndexedSortedMapStorageNode){
			IndexedSortedMapStorageNode<PK,D,F> typedMaster = (IndexedSortedMapStorageNode<PK,D,F>)master;
			List<IndexedSortedMapStorageNode<PK,D,F>> typedSlaves = (List<IndexedSortedMapStorageNode<PK,D,F>>)slaves;
			return (N)new MasterSlaveIndexedSortedMapStorageNode<>(typedMaster, typedSlaves);
		}
		if(master instanceof SortedMapStorageNode){
			SortedMapStorageNode<PK,D,F> typedMaster = (SortedMapStorageNode<PK,D,F>)master;
			List<SortedMapStorageNode<PK,D,F>> typedSlaves = (List<SortedMapStorageNode<PK,D,F>>)slaves;
			return (N)new MasterSlaveSortedMapStorageNode<>(typedMaster, typedSlaves);
		}
		if(master instanceof MapStorageNode){
			MapStorageNode<PK,D,F> typedMaster = (MapStorageNode<PK,D,F>)master;
			List<MapStorageNode<PK,D,F>> typedSlaves = (List<MapStorageNode<PK,D,F>>)slaves;
			return (N)new MasterSlaveMapStorageNode<>(typedMaster, typedSlaves);
		}
		throw new UnsupportedOperationException("No MasterSlave implementation found for " + master.getClass());
	}

}
