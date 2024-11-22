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

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.timer.PhaseTimer;
import net.spy.memcached.compat.log.SLF4JLogger;

public abstract class BaseMemcachedClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(BaseMemcachedClientManager.class);

	private final MemcachedClientHolder clientHolder;

	protected BaseMemcachedClientManager(MemcachedClientHolder spyMemcachedClientHolder){
		this.clientHolder = spyMemcachedClientHolder;
	}

	protected abstract DatarouterMemcachedClient buildClient(ClientId clientId);

	@Override
	protected void safeInitClient(ClientId clientId){
		logger.info("{} activating client={}", getClass().getSimpleName(), clientId.getName());
		var timer = new PhaseTimer(clientId.getName());
		// Configure logging before any call to spy.memcached
		System.setProperty("net.spy.log.LoggerImpl", SLF4JLogger.class.getName());
		clientHolder.register(clientId, buildClient(clientId));
		logger.warn("{}", timer.add("done"));
	}

	@Override
	public void shutdown(ClientId clientId){
		clientHolder.get(clientId).getSpyClient().shutdown();
	}

	public DatarouterMemcachedClient getClient(ClientId clientId){
		initClient(clientId);
		return clientHolder.get(clientId);
	}

	public Supplier<DatarouterMemcachedClient> getLazyClient(ClientId clientId){
		return SingletonSupplier.of(() -> getClient(clientId));
	}

}
