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
package io.datarouter.client.memcached.client;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.memcached.client.options.MemcachedOptions;
import io.datarouter.client.memcached.client.spy.SpyMemcachedClient;
import io.datarouter.client.memcached.client.spy.SpyMemcachedClientHolder;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.timer.PhaseTimer;
import net.spy.memcached.KetamaConnectionFactory;
import net.spy.memcached.compat.log.SLF4JLogger;

@Singleton
public class MemcachedClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedClientManager.class);

	@Inject
	private SpyMemcachedClientHolder spyMemcachedClientHolder;
	@Inject
	private MemcachedOptions memcachedOptions;

	@Override
	public void shutdown(ClientId clientId){
		spyMemcachedClientHolder.get(clientId).shutdown();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		logger.info("activating BaseMemcached client " + clientId.getName());
		PhaseTimer timer = new PhaseTimer(clientId.getName());
		// Configure logging before any call to spy.memcached
		System.setProperty("net.spy.log.LoggerImpl", SLF4JLogger.class.getName());
		try{
			spyMemcachedClientHolder.register(clientId, buildSpyClient(clientId));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		logger.warn(timer.add("done").toString());
	}

	protected SpyMemcachedClient buildSpyClient(ClientId clientId) throws IOException{
		KetamaConnectionFactory connectionFactory = new KetamaConnectionFactory(){
			@Override
			public long getOperationTimeout(){
				return 200;
			}
		};
		return new SpyMemcachedClient(connectionFactory, memcachedOptions.getServers(clientId.getName()));
	}

	public SpyMemcachedClient getSpyMemcachedClient(ClientId clientId){
		initClient(clientId);
		return spyMemcachedClientHolder.get(clientId);
	}

}
