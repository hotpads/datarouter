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
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.TallyNodeFactory;
import io.datarouter.storage.node.op.NodeOps;

public class TallyNodeBuilder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final Datarouter datarouter;
	private final TallyNodeFactory nodeFactory;
	private final ClientId clientId;
	private final Supplier<D> databeanSupplier;
	private final Supplier<F> fielderSupplier;

	private int version;
	private String tableName;
	private boolean isSystemTable;

	public TallyNodeBuilder(
			Datarouter datarouter,
			TallyNodeFactory nodeFactory,
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		this.datarouter = datarouter;
		this.nodeFactory = nodeFactory;
		this.clientId = clientId;
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
	}

	public TallyNodeBuilder<PK,D,F> withSchemaVersion(int version){
		this.version = version;
		return this;
	}

	public TallyNodeBuilder<PK,D,F> withTableName(String tableName){
		this.tableName = tableName;
		return this;
	}

	public TallyNodeBuilder<PK,D,F> withIsSystemTable(boolean isSystemTable){
		this.isSystemTable = isSystemTable;
		return this;
	}

	public <N extends NodeOps<PK,D>> N build(){
		return nodeFactory.createTallyNode(clientId, databeanSupplier, fielderSupplier, version, tableName,
				isSystemTable);
	}

	public <N extends NodeOps<PK,D>> N buildAndRegister(){
		return datarouter.register(build());
	}

}
