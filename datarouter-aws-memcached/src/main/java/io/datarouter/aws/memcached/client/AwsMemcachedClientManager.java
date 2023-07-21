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
package io.datarouter.aws.memcached.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import io.datarouter.aws.memcached.client.options.AwsMemcachedOptions;
import io.datarouter.client.memcached.client.BaseMemcachedClientManager;
import io.datarouter.client.memcached.client.DatarouterMemcachedClient;
import io.datarouter.client.memcached.client.MemcachedClientHolder;
import io.datarouter.client.memcached.client.SpyMemcachedClient;
import io.datarouter.storage.client.ClientId;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.KetamaConnectionFactory;

@Singleton
public class AwsMemcachedClientManager extends BaseMemcachedClientManager{

	@Inject
	private AwsMemcachedOptions options;

	@Inject
	public AwsMemcachedClientManager(
			MemcachedClientHolder clientHolder,
			AwsMemcachedOptions options){
		super(clientHolder);
		this.options = options;
	}

	@Override
	protected DatarouterMemcachedClient buildClient(ClientId clientId){
		MemcachedClientMode clientMode = options.getClientMode(clientId.getName());
		// use KetamaConnectionFactory for consistent hashing between memcached nodes
		var ketamaConnectionFactory = new KetamaConnectionFactory(
				clientMode.clientMode,
				DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN,
				DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE,
				DefaultConnectionFactory.DEFAULT_OP_QUEUE_MAX_BLOCK_TIME){
			@Override
			public long getOperationTimeout(){
				return 200;
			}
		};

		List<InetSocketAddress> addresses;
		if(clientMode == MemcachedClientMode.DYNAMIC){
			// builds aws-memcached-client with cluster endpoint and enable auto-discovery
			addresses = options.getClusterEndpoint(clientId.getName())
					.map(List::of)
					.get();
		}else{
			// builds aws-memcached-client with list of nodes and does not enable auto-discovery
			// builds memcached-client with list of nodes
			addresses = options.getServers(clientId.getName());
		}
		try{
			var spyClient = new SpyMemcachedClient(ketamaConnectionFactory, addresses);
			return new DatarouterMemcachedClient(spyClient);
		}catch(RuntimeException | IOException e){
			throw new RuntimeException("failed to build memcached client for " + addresses, e);
		}
	}

}
