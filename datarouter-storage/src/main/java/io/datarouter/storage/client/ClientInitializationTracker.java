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
package io.datarouter.storage.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Singleton;

@Singleton
public class ClientInitializationTracker{

	private final Set<ClientId> initializedClients = ConcurrentHashMap.newKeySet();

	public void setInitialized(ClientId clientId){
		if(!initializedClients.add(clientId)){
			throw new RuntimeException(clientId + " was already initialized");
		}
	}

	public boolean isInitialized(ClientId clientId){
		return initializedClients.contains(clientId);
	}

	public Set<ClientId> getInitializedClients(){
		return initializedClients;
	}

}
