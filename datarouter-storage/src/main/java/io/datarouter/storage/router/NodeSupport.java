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
package io.datarouter.storage.router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.Node;

public interface NodeSupport{

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D,F>>
	N register(N node);

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D,F>>
	N createAndBuild(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier);

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D,F>>
	N createAndRegister(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier);

	default <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D,F>>
	List<N> createAndBuildMulti(Collection<ClientId> clientIds, Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		List<N> nodes = new ArrayList<>();
		for(ClientId clientId : clientIds){
			nodes.add(createAndBuild(clientId, databeanSupplier, fielderSupplier));
		}
		return nodes;
	}

}
