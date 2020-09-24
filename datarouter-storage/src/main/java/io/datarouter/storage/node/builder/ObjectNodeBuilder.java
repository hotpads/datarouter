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
import io.datarouter.storage.node.factory.ObjectNodeFactory;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.util.Subpath;

public class ObjectNodeBuilder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	protected final Datarouter datarouter;
	protected final ObjectNodeFactory objectNodeFactory;
	protected final ClientId clientId;
	protected final Supplier<D> databeanSupplier;
	protected final Supplier<F> fielderSupplier;

	protected String bucket;
	protected Subpath path;

	public ObjectNodeBuilder(
			Datarouter datarouter,
			ObjectNodeFactory objectNodeFactory,
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		this.datarouter = datarouter;
		this.objectNodeFactory = objectNodeFactory;
		this.clientId = clientId;
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
	}

	public <N extends NodeOps<PK,D>> N build(){
		return objectNodeFactory.create(
				clientId,
				databeanSupplier,
				fielderSupplier,
				bucket,
				path);
	}

}