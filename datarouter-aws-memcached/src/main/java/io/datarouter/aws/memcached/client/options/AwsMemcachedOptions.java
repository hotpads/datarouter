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
package io.datarouter.aws.memcached.client.options;

import java.net.InetSocketAddress;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.memcached.client.MemcachedClientMode;
import io.datarouter.client.memcached.client.options.MemcachedOptions;
import io.datarouter.storage.client.ClientOptions;

@Singleton
public class AwsMemcachedOptions extends MemcachedOptions{

	public static final String PROP_clientMode = "clientMode";
	public static final String PROP_clusterEndpoint = "clusterEndpoint";

	@Inject
	private ClientOptions clientOptions;

	public MemcachedClientMode getClientMode(String clientName){
		return clientOptions.optString(clientName, makeAwsMemcachedKey(PROP_clientMode))
				.map(MemcachedClientMode::fromPersistentStringStatic)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.orElse(MemcachedClientMode.STATIC);
	}

	public Optional<InetSocketAddress> getClusterEndpoint(String clientName){
		return clientOptions.optInetSocketAddress(clientName, makeAwsMemcachedKey(PROP_clusterEndpoint));
	}

	public static String makeAwsMemcachedKey(String propertyKey){
		return "awsMemcached." + propertyKey;
	}

}
