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

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ReplicationClientIds;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.virtualnode.replication.ReplicationNodeOptions.ReplicationNodeOptionsBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ReplicationNodeFactory{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	/*-------------- build ---------------*/

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ReplicationClientIds replicationClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		var options = new ReplicationNodeOptionsBuilder().build();
		return build(replicationClientIds, databeanSupplier, fielderSupplier, options);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N build(
			ReplicationClientIds replicationClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			ReplicationNodeOptions options){
		var primaryBuilder = nodeFactory.create(replicationClientIds.primary, databeanSupplier, fielderSupplier);
		options.tableName.ifPresent(primaryBuilder::withTableName);
		options.disableForcePrimary.ifPresent(primaryBuilder::withDisableForcePrimary);
		options.disableIntroducer.ifPresent(_ -> primaryBuilder.disableIntroducer());
		options.nodewatchConfigurationBuilder.ifPresent(primaryBuilder::withNodewatchConfigurationBuilder);
		primaryBuilder.withPrivacyProcessors(options.privacyProcessors);
		options.privacyExemptionReason.ifPresent(primaryBuilder::withPrivacyExemptionReason);
		N primary = primaryBuilder.build();
		List<N> replicas = Scanner.of(replicationClientIds.replicas)
				.map(clientId -> {
					var replicaBuilder = nodeFactory.create(clientId, databeanSupplier, fielderSupplier);
					options.tableName.ifPresent(replicaBuilder::withTableName);
					options.disableForcePrimary.ifPresent(replicaBuilder::withDisableForcePrimary);
					options.disableIntroducer.ifPresent(_ -> replicaBuilder.disableIntroducer());
					replicaBuilder.withPrivacyProcessors(options.privacyProcessors);
					options.privacyExemptionReason.ifPresent(replicaBuilder::withPrivacyExemptionReason);
					N replicaNode = replicaBuilder.build();
					return replicaNode;
				})
				.list();
		return makeInternal(primary, replicas, options.everyNToPrimary.orElse(null));
	}

	/*------------- build with EntityKey ------------*/

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
		N build(
			ReplicationClientIds replicationClientIds,
			Supplier<EK> entityKeySupplier,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		var options = new ReplicationNodeOptionsBuilder()
				.build();
		return build(replicationClientIds, entityKeySupplier, databeanSupplier, fielderSupplier, options);
	}

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
		N build(
			ReplicationClientIds replicationClientIds,
			Supplier<EK> entityKeySupplier,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			ReplicationNodeOptions options){
		var primaryBuilder = nodeFactory.create(replicationClientIds.primary, entityKeySupplier, databeanSupplier,
				fielderSupplier);
		options.tableName.ifPresent(primaryBuilder::withTableName);
		options.disableForcePrimary.ifPresent(primaryBuilder::withDisableForcePrimary);
		options.disableIntroducer.ifPresent(_ -> primaryBuilder.disableIntroducer());
		options.nodewatchConfigurationBuilder.ifPresent(primaryBuilder::withNodewatchConfigurationBuilder);
		primaryBuilder.withPrivacyProcessors(options.privacyProcessors);
		options.privacyExemptionReason.ifPresent(primaryBuilder::withPrivacyExemptionReason);
		N primary = primaryBuilder.build();
		List<N> replicas = Scanner.of(replicationClientIds.replicas)
				.map(clientId -> {
					var replicaBuilder = nodeFactory.create(clientId, entityKeySupplier, databeanSupplier,
							fielderSupplier);
					options.tableName.ifPresent(replicaBuilder::withTableName);
					options.disableForcePrimary.ifPresent(replicaBuilder::withDisableForcePrimary);
					options.disableIntroducer.ifPresent(_ -> replicaBuilder.disableIntroducer());
					replicaBuilder.withPrivacyProcessors(options.privacyProcessors);
					options.privacyExemptionReason.ifPresent(replicaBuilder::withPrivacyExemptionReason);
					N replicaNode = replicaBuilder.build();
					return replicaNode;
				})
				.list();
		return makeInternal(primary, replicas, options.everyNToPrimary.orElse(null));
	}

	/*------------ register --------------*/

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N register(
			ReplicationClientIds replicationClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return datarouter.register(build(replicationClientIds, databeanSupplier, fielderSupplier));
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N register(
			ReplicationClientIds replicationClientIds,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			ReplicationNodeOptions options){
		return datarouter.register(build(
				replicationClientIds,
				databeanSupplier,
				fielderSupplier,
				options));
	}

	/*-------------- private --------------*/

	@SuppressWarnings("unchecked")
	private static <
			PK extends EntityPrimaryKey<?,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N makeInternal(N primary, List<N> replicas, Integer everyNToPrimary){
		if(primary instanceof IndexedSortedMapStorageNode){
			IndexedSortedMapStorageNode<PK,D,F> typedPrimary = (IndexedSortedMapStorageNode<PK,D,F>)primary;
			List<IndexedSortedMapStorageNode<PK,D,F>> typedReplicas = (List<IndexedSortedMapStorageNode<PK,D,F>>)
					replicas;
			return (N)new ReplicationIndexedSortedMapStorageNode<>(typedPrimary, typedReplicas, everyNToPrimary);
		}
		if(primary instanceof SortedMapStorageNode){
			SortedMapStorageNode<PK,D,F> typedPrimary = (SortedMapStorageNode<PK,D,F>)primary;
			List<SortedMapStorageNode<PK,D,F>> typedReplicas = (List<SortedMapStorageNode<PK,D,F>>)replicas;
			return (N)new ReplicationSortedMapStorageNode<>(typedPrimary, typedReplicas, everyNToPrimary);
		}
		if(primary instanceof MapStorageNode){
			MapStorageNode<PK,D,F> typedPrimary = (MapStorageNode<PK,D,F>)primary;
			List<MapStorageNode<PK,D,F>> typedReplicas = (List<MapStorageNode<PK,D,F>>)replicas;
			return (N)new ReplicationMapStorageNode<>(typedPrimary, typedReplicas, everyNToPrimary);
		}
		throw new UnsupportedOperationException("No ReplicationNode implementation found for " + primary.getClass());
	}

}
