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
package io.datarouter.storage.node.factory;

import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.imp.StreamClientNodeFactory;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import jakarta.inject.Singleton;

@Singleton
public class StreamNodeFactory extends BaseNodeFactory{

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createSingleStreamNode(
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			String streamName){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withStreamName(streamName)
				.withTableName(streamName)
				.build();
		StreamClientNodeFactory clientFactories = getClientNodeFactory(clientId, StreamClientNodeFactory.class);
		return cast(clientFactories.createSingleStreamNode(params));
	}

}
