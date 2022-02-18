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
package io.datarouter.storage.config.client;

import java.util.List;

import io.datarouter.storage.client.ClientId;

public class MemcachedGenericClientOptions{

	public enum MemcachedGenericClientMode{
		STATIC,
		DYNAMIC;
	}

	public final ClientId clientId;
	public final MemcachedGenericClientMode clientMode;
	public List<String> servers;
	public String clusterEndpoint;

	public MemcachedGenericClientOptions(ClientId clientId, MemcachedGenericClientMode clientMode){
		this.clientId = clientId;
		this.clientMode = clientMode;
	}

	public MemcachedGenericClientOptions withServers(String... servers){
		this.servers = List.of(servers);
		return this;
	}

	public MemcachedGenericClientOptions withServers(List<String> servers){
		this.servers = servers;
		return this;
	}

	public MemcachedGenericClientOptions withClusterEndpoint(String clusterEndpoint){
		this.clusterEndpoint = clusterEndpoint;
		return this;
	}

}
