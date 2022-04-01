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
package io.datarouter.client.memcached.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.memcached.client.options.MemcachedOptions;
import io.datarouter.storage.client.ClientId;
import net.spy.memcached.KetamaConnectionFactory;

@Singleton
public class MemcachedClientManager extends BaseMemcachedClientManager{

	private final MemcachedOptions options;

	@Inject
	public MemcachedClientManager(
			MemcachedClientHolder clientHolder,
			MemcachedOptions options){
		super(clientHolder);
		this.options = options;
	}


	@Override
	protected DatarouterMemcachedClient buildClient(ClientId clientId){
		KetamaConnectionFactory connectionFactory = new KetamaConnectionFactory(){
			@Override
			public long getOperationTimeout(){
				return 200;
			}
		};
		List<InetSocketAddress> addresses = options.getServers(clientId.getName());
		try{
			var spyClient = new SpyMemcachedClient(connectionFactory, options.getServers(clientId.getName()));
			return new DatarouterMemcachedClient(spyClient);
		}catch(RuntimeException | IOException e){
			throw new RuntimeException("failed to build memcached client for " + addresses, e);
		}
	}

}
