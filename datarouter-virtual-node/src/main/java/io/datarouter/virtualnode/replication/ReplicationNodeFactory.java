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
package io.datarouter.virtualnode.replication;

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
public class ReplicationNodeFactory{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId primaryClientId,
			Collection<ClientId> replicaClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
	N primary = nodeFactory.create(primaryClientId, databeanSupplier, fielderSupplier).build();
		Function<ClientId,N> buildReplicaFunction = clientId -> nodeFactory.create(clientId, databeanSupplier,
				fielderSupplier).build();
		List<N> replicas = Scanner.of(replicaClientIds).map(buildReplicaFunction).list();
		return make(primary, replicas);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId primaryClientId,
			Collection<ClientId> replicaClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			String tableName){
		N primary = nodeFactory.create(primaryClientId, databeanSupplier, fielderSupplier)
				.withTableName(tableName)
				.build();
		Function<ClientId,N> buildReplicaFunction = clientId -> nodeFactory.create(clientId, databeanSupplier,
				fielderSupplier)
				.withTableName(tableName)
				.build();
		List<N> replicas = Scanner.of(replicaClientIds).map(buildReplicaFunction).list();
		return make(primary, replicas);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId primaryClientId,
			Collection<ClientId> replicaClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			NodewatchConfigurationBuilder nodewatchConfigurationBuilder){
		N primary = nodeFactory.create(primaryClientId, databeanSupplier, fielderSupplier)
				.withNodewatchConfigurationBuilder(nodewatchConfigurationBuilder)
				.build();
		Function<ClientId,N> buildReplicaFunction = clientId -> nodeFactory.create(clientId, databeanSupplier,
				fielderSupplier).build();
		List<N> replicas = Scanner.of(replicaClientIds).map(buildReplicaFunction).list();
		return make(primary, replicas);
	}

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId primaryClientId,
			Collection<ClientId> replicaClientIds,
			Supplier<EK> entityKeySupplier,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		N primary = nodeFactory.create(primaryClientId, entityKeySupplier, databeanSupplier, fielderSupplier).build();
		Function<ClientId,N> buildReplicaFunction = clientId -> nodeFactory.create(clientId, entityKeySupplier,
				databeanSupplier, fielderSupplier).build();
		List<N> replicas = Scanner.of(replicaClientIds).map(buildReplicaFunction).list();
		return make(primary, replicas);
	}

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ClientId primaryClientId,
			Collection<ClientId> replicaClientIds,
			Supplier<EK> entityKeySupplier,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			NodewatchConfigurationBuilder nodewatchConfigurationBuilder){
		N primary = nodeFactory.create(primaryClientId, entityKeySupplier, databeanSupplier, fielderSupplier)
				.withNodewatchConfigurationBuilder(nodewatchConfigurationBuilder)
				.build();
		Function<ClientId,N> buildReplicaFunction = clientId -> nodeFactory.create(clientId, entityKeySupplier,
				databeanSupplier, fielderSupplier).build();
		List<N> replicas = Scanner.of(replicaClientIds).map(buildReplicaFunction).list();
		return make(primary, replicas);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N register(
			ClientId primaryClientId,
			Collection<ClientId> replicaClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return datarouter.register(build(primaryClientId, replicaClientIds, databeanSupplier, fielderSupplier));
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N register(
			ClientId primaryClientId,
			Collection<ClientId> replicaClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			String tableName){
		return datarouter.register(build(primaryClientId, replicaClientIds, databeanSupplier, fielderSupplier,
				tableName));
	}

	@SuppressWarnings("unchecked")
	public static <
			PK extends EntityPrimaryKey<?,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N make(N primary, List<N> replicas){
		if(primary instanceof IndexedSortedMapStorageNode){
			IndexedSortedMapStorageNode<PK,D,F> typedPrimary = (IndexedSortedMapStorageNode<PK,D,F>)primary;
			List<IndexedSortedMapStorageNode<PK,D,F>> typedReplicas = (List<IndexedSortedMapStorageNode<PK,D,F>>)
					replicas;
			return (N)new ReplicationIndexedSortedMapStorageNode<>(typedPrimary, typedReplicas);
		}
		if(primary instanceof SortedMapStorageNode){
			SortedMapStorageNode<PK,D,F> typedPrimary = (SortedMapStorageNode<PK,D,F>)primary;
			List<SortedMapStorageNode<PK,D,F>> typedReplicas = (List<SortedMapStorageNode<PK,D,F>>)replicas;
			return (N)new ReplicationSortedMapStorageNode<>(typedPrimary, typedReplicas);
		}
		if(primary instanceof MapStorageNode){
			MapStorageNode<PK,D,F> typedPrimary = (MapStorageNode<PK,D,F>)primary;
			List<MapStorageNode<PK,D,F>> typedReplicas = (List<MapStorageNode<PK,D,F>>)replicas;
			return (N)new ReplicationMapStorageNode<>(typedPrimary, typedReplicas);
		}
		throw new UnsupportedOperationException("No ReplicationNode implementation found for " + primary.getClass());
	}

}
