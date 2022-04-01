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
package io.datarouter.storage.node.factory;

import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.imp.DatabeanClientNodeFactory;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.builder.NodeBuilder;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public abstract class BaseDatabeanNodeFactory extends BaseNodeFactory{

	private final Supplier<Boolean> enableDiagnosticsSupplier;

	public BaseDatabeanNodeFactory(Supplier<Boolean> enableDiagnosticsSupplier){
		this.enableDiagnosticsSupplier = enableDiagnosticsSupplier;
	}

	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends PhysicalNode<PK,D,F>>
	N create(
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> params){
		DatabeanClientNodeFactory clientNodeFactory = getClientNodeFactory(
				params.getClientId(),
				DatabeanClientNodeFactory.class);
		return cast(clientNodeFactory.createDatabeanNode(entityNodeParams, params));
	}

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	NodeBuilder<EK,PK,D,F> create(
			ClientId clientId,
			Supplier<EK> entityKeySupplier,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new NodeBuilder<>(this, enableDiagnosticsSupplier, clientId, entityKeySupplier,
				databeanSupplier, fielderSupplier);
	}

	public <PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	NodeBuilder<PK,PK,D,F> create(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		Supplier<PK> entityKeySupplier = databeanSupplier.get().getKeySupplier();
		return create(clientId, entityKeySupplier, databeanSupplier, fielderSupplier);
	}

	public <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends PhysicalNode<PK,D,F>>
	N register(N node){
		return datarouter.register(node);
	}

}
