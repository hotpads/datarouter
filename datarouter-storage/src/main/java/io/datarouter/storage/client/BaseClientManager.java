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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;

import io.datarouter.storage.node.type.physical.PhysicalNode;

public abstract class BaseClientManager implements ClientManager{

	@Inject
	private ClientInitializationTracker clientInitializationTracker;

	@Override
	public boolean monitorLatency(){
		return true;
	}

	@Override
	public Future<Optional<SchemaUpdateResult>> notifyNodeRegistration(PhysicalNode<?,?,?> node){
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public final void initClient(ClientId clientId){
		clientInitializationTracker.initClient(clientId, this::safeInitClient, this::notifyNodeRegistration,
				this::sendEmail);
	}

	protected abstract void safeInitClient(ClientId clientId);

	@Override
	public void sendEmail(){};

}
