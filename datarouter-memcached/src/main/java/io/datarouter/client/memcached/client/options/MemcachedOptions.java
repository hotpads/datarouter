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
package io.datarouter.client.memcached.client.options;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import io.datarouter.storage.client.ClientOptions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MemcachedOptions{

	private static final String PREFIX_MEMCACHED = "memcached.";

	protected static final String PROP_numServers = "numServers";
	protected static final String PROP_server = "server";

	@Inject
	private ClientOptions clientOptions;

	public List<InetSocketAddress> getServers(String clientName){
		return IntStream.range(0, getNumServers(clientName))
				.mapToObj(index -> makeMemcachedKey(PROP_server + "." + index))
				.map(propertyKey -> clientOptions.optInetSocketAddress(clientName, propertyKey))
				.map(Optional::get)
				.toList();
	}

	private Integer getNumServers(String clientName){
		return clientOptions.getRequiredInteger(clientName, makeMemcachedKey(PROP_numServers));
	}

	protected static String makeMemcachedKey(String propertyKey){
		return PREFIX_MEMCACHED + propertyKey;
	}

}
