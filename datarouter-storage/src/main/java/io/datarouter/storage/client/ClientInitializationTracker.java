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
package io.datarouter.storage.client;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.OptionalTool;
import io.datarouter.util.concurrent.FutureTool;

@Singleton
public class ClientInitializationTracker{

	@Inject
	private DatarouterNodes datarouterNodes;

	private final Set<ClientId> initializedClients = ConcurrentHashMap.newKeySet();

	public void initClient(ClientId clientId, Consumer<ClientId> initializer,
			Function<PhysicalNode<?,?,?>,Future<Optional<SchemaUpdateResult>>> nodeRegistrationNotifier,
			Runnable emailer){
		if(initializedClients.contains(clientId)){
			return;
		}
		synchronized(initializedClients){
			if(initializedClients.contains(clientId)){
				return;
			}
			initializer.accept(clientId);
			initializedClients.add(clientId);
			List<Optional<SchemaUpdateResult>> dtos = datarouterNodes.getPhysicalNodesForClient(clientId.getName())
					.stream()
					.map(nodeRegistrationNotifier::apply)
					.collect(Collectors.collectingAndThen(Collectors.toList(), FutureTool::getAll));

			dtos.stream()
					.flatMap(OptionalTool::stream)
					.map(result -> result.errorMessage)
					.flatMap(OptionalTool::stream)
					.findFirst()
					.ifPresent($ -> emailer.run());
		}
	}

	public boolean isInitialized(ClientId clientId){
		return initializedClients.contains(clientId);
	}

	public Set<ClientId> getInitializedClients(){
		return initializedClients;
	}

}
