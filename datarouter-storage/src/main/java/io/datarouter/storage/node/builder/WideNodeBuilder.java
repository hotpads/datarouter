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
import io.datarouter.model.key.entity.base.DefaultEntityPartitioner;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.setting.DatarouterStorageSettingRoot;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.entity.DefaultEntity;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.util.lang.ReflectionTool;

public class WideNodeBuilder<
		PK extends RegularPrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final Datarouter datarouter;
	private final WideNodeFactory wideNodeFactory;
	private final DatarouterStorageSettingRoot storageSettings;
	private final ClientId clientId;
	private final Supplier<PK> entityKeySupplier;
	private final Supplier<D> databeanSupplier;
	private final Supplier<F> fielderSupplier;
	private String tableName;
	private Integer schemaVersion;

	public WideNodeBuilder(
			Datarouter datarouter,
			WideNodeFactory wideNodeFactory,
			DatarouterStorageSettingRoot storageSettings,
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		this.datarouter = datarouter;
		this.wideNodeFactory = wideNodeFactory;
		this.storageSettings = storageSettings;
		this.clientId = clientId;
		this.entityKeySupplier = () -> ReflectionTool.create(databeanSupplier.get().getKeyClass());
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
	}

	public WideNodeBuilder<PK,D,F> withTableName(String tableName){
		this.tableName = tableName;
		return this;
	}

	public WideNodeBuilder<PK,D,F> withSchemaVersion(Integer schemaVersion){
		this.schemaVersion = schemaVersion;
		return this;
	}

	public <N extends NodeOps<PK,D>> N build(){
		String databeanName = databeanSupplier.get().getDatabeanName();
		String entityName = databeanName + "Entity";
		String entityNodePrefix = databeanName.replaceAll("[a-z]", "");
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withDiagnostics(storageSettings.recordCallsites)
				.withEntity(entityName, entityNodePrefix)
				.withParentName(entityName)
				.withTableName(tableName != null ? tableName : databeanName)
				.withSchemaVersion(schemaVersion)
				.build();
		EntityNodeParams<PK,DefaultEntity<PK>> entityNodeParams = new EntityNodeParams<>(
				clientId.getName() + "." + entityName,
				entityKeySupplier,
				DefaultEntity.supplier(entityKeySupplier),
				DefaultEntityPartitioner::new,
				entityName);
		return wideNodeFactory.createSubEntity(entityNodeParams, params);
	}

	public <N extends NodeOps<PK,D>> N buildAndRegister(){
		return datarouter.register(build());
	}

}