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

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;

import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.concurrent.FutureTool;

public abstract class BaseClientManager implements ClientManager{

	@Inject
	private ClientInitializationTracker clientInitializationTracker;
	@Inject
	private DatarouterNodes datarouterNodes;

	@Override
	public boolean monitorLatency(){
		return true;
	}

	protected Future<Optional<SchemaUpdateResult>> doSchemaUpdate(@SuppressWarnings("unused") PhysicalNode<?,?,?> node){
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public void doSchemaUpdate(Collection<? extends PhysicalNode<?,?,?>> nodes){
		nodes.stream()
				.map(this::doSchemaUpdate)
				.map(FutureTool::get)
				.flatMap(Optional::stream)
				.map(result -> result.startupBlockReason)
				.flatMap(Optional::stream)
				.findFirst()
				.ifPresent($ -> gatherSchemaUpdates());
	}

	@Override
	public final void initClient(ClientId clientId){
		if(clientInitializationTracker.isInitialized(clientId)){
			return;
		}
		synchronized(clientId){
			if(clientInitializationTracker.isInitialized(clientId)){
				return;
			}
			safeInitClient(clientId);
			clientInitializationTracker.setInitialized(clientId);
			doSchemaUpdate(datarouterNodes.getPhysicalNodesForClient(clientId.getName()));
		}
	}

	protected abstract void safeInitClient(ClientId clientId);

	@Override
	public void gatherSchemaUpdates(){}

}
