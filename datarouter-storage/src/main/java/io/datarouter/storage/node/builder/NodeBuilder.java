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
package io.datarouter.storage.node.builder;

import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.entity.base.DefaultEntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.entity.DefaultEntity;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.factory.BaseNodeFactory;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.storage.node.tableconfig.NodewatchConfiguration;
import io.datarouter.storage.node.tableconfig.NodewatchConfigurationBuilder;

public class NodeBuilder<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final BaseNodeFactory nodeFactory;
	private final Supplier<Boolean> enableDiagnosticsSupplier;
	private final ClientId clientId;
	private final Supplier<EK> entityKeySupplier;
	private final Supplier<D> databeanSupplier;
	private final Supplier<F> fielderSupplier;
	private Supplier<EntityPartitioner<EK>> partitionerSupplier;
	private String tableName;
	private Integer schemaVersion;
	private NodewatchConfigurationBuilder nodewatchConfigurationBuilder;
	private boolean disableForcePrimary;
	private boolean isSystemTable;
	private boolean disableIntroducer = false;

	public NodeBuilder(
			BaseNodeFactory nodeFactory,
			Supplier<Boolean> enableDiagnosticsSupplier,
			ClientId clientId,
			Supplier<EK> entityKeySupplier,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		this.nodeFactory = nodeFactory;
		this.enableDiagnosticsSupplier = enableDiagnosticsSupplier;
		this.clientId = clientId;
		this.entityKeySupplier = entityKeySupplier;
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
		this.partitionerSupplier = DefaultEntityPartitioner::new;
	}

	public NodeBuilder<EK,PK,D,F> withPartitionerSupplier(Supplier<EntityPartitioner<EK>> pertitionerSupplier){
		this.partitionerSupplier = pertitionerSupplier;
		return this;
	}

	public NodeBuilder<EK,PK,D,F> withTableName(String tableName){
		this.tableName = tableName;
		return this;
	}

	public NodeBuilder<EK,PK,D,F> withSchemaVersion(Integer schemaVersion){
		this.schemaVersion = schemaVersion;
		return this;
	}

	public NodeBuilder<EK,PK,D,F> withDisableForcePrimary(boolean disableForcePrimary){
		this.disableForcePrimary = disableForcePrimary;
		return this;
	}

	public NodeBuilder<EK,PK,D,F> withIsSystemTable(boolean isSystemTable){
		this.isSystemTable = isSystemTable;
		return this;
	}

	public NodeBuilder<EK,PK,D,F> disableIntroducer(){
		this.disableIntroducer = true;
		return this;
	}

	/*---------- Nodewatch ----------*/

	public NodeBuilder<EK,PK,D,F> withNodewatchConfigurationBuilder(
			NodewatchConfigurationBuilder nodewatchConfigurationBuilder){
		this.nodewatchConfigurationBuilder = nodewatchConfigurationBuilder;
		return this;
	}

	public NodeBuilder<EK,PK,D,F> withNodewatchThreshold(long threshold){
		if(nodewatchConfigurationBuilder == null){
			nodewatchConfigurationBuilder = new NodewatchConfigurationBuilder();
		}
		nodewatchConfigurationBuilder.withThreshold(threshold);
		return this;
	}

	public NodeBuilder<EK,PK,D,F> withNodewatchSampleSize(int sampleSize){
		if(nodewatchConfigurationBuilder == null){
			nodewatchConfigurationBuilder = new NodewatchConfigurationBuilder();
		}
		nodewatchConfigurationBuilder.withSampleSize(sampleSize);
		return this;
	}

	public NodeBuilder<EK,PK,D,F> withNodewatchBatchSize(int batchSize){
		if(nodewatchConfigurationBuilder == null){
			nodewatchConfigurationBuilder = new NodewatchConfigurationBuilder();
		}
		nodewatchConfigurationBuilder.withBatchSize(batchSize);
		return this;
	}

	public NodeBuilder<EK,PK,D,F> disableNodewatch(){
		if(nodewatchConfigurationBuilder == null){
			nodewatchConfigurationBuilder = new NodewatchConfigurationBuilder();
		}
		nodewatchConfigurationBuilder.disable();
		return this;
	}

	public NodeBuilder<EK,PK,D,F> disableNodewatchPercentageAlert(){
		if(nodewatchConfigurationBuilder == null){
			nodewatchConfigurationBuilder = new NodewatchConfigurationBuilder();
		}
		nodewatchConfigurationBuilder.disablePercentChangeAlert();
		return this;
	}

	public NodeBuilder<EK,PK,D,F> disableNodewatchThresholdAlert(){
		if(nodewatchConfigurationBuilder == null){
			nodewatchConfigurationBuilder = new NodewatchConfigurationBuilder();
		}
		nodewatchConfigurationBuilder.disableMaxThresholdAlert();
		return this;
	}

	public <N extends NodeOps<PK,D>> N build(){
		String databeanName = databeanSupplier.get().getDatabeanName();
		String entityName = databeanName + "Entity";
		String entityNodePrefix = null;
		String nodeTableName = tableName != null ? tableName : databeanName;
		NodewatchConfiguration tableConfig = null;
		if(nodewatchConfigurationBuilder != null){
			tableConfig = nodewatchConfigurationBuilder.create(new ClientTableEntityPrefixNameWrapper(
					clientId.getName(),
					nodeTableName,
					entityNodePrefix));
		}
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withDiagnostics(enableDiagnosticsSupplier)
				.withEntity(entityName, entityNodePrefix)
				.withParentName(entityName)
				.withTableName(nodeTableName)
				.withSchemaVersion(schemaVersion)
				.withTableConfiguration(tableConfig)
				.withDisableForcePrimary(disableForcePrimary)
				.withIsSystemTable(isSystemTable)
				.withDisableIntroducer(disableIntroducer)
				.build();
		EntityNodeParams<EK,DefaultEntity<EK>> entityNodeParams = new EntityNodeParams<>(
				clientId.getName() + "." + entityName,
				entityKeySupplier,
				DefaultEntity.supplier(entityKeySupplier),
				partitionerSupplier,
				entityName);
		return nodeFactory.create(entityNodeParams, params);
	}

	public <N extends NodeOps<PK,D>> N buildAndRegister(){
		return nodeFactory.register(build());
	}

}
