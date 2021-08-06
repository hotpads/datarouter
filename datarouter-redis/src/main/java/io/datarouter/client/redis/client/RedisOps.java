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
package io.datarouter.client.redis.client;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Twin;
import io.lettuce.core.KeyValue;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

public class RedisOps{
	private static final Logger logger = LoggerFactory.getLogger(RedisOps.class);

	private final RedisClusterAsyncCommands<byte[],byte[]> client;

	public RedisOps(RedisClusterAsyncCommands<byte[],byte[]> client){
		this.client = client;
	}

	public boolean exists(byte[] key){
		try{
			return client.exists(key).get() == 1;
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
			return false;
		}
	}

	public Optional<byte[]> find(byte[] key){
		try{
			return Optional.ofNullable(client.get(key).get());
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
			return Optional.empty();
		}
	}

	public Scanner<KeyValue<byte[],byte[]>> mget(List<byte[]> keys){
		byte[][] keysArray = keys.toArray(new byte[keys.size()][]);
		try{
			return Scanner.of(client.mget(keysArray).get());
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
			return Scanner.empty();
		}
	}

	public void set(Twin<byte[]> kv){
		try{
			client.set(kv.getLeft(), kv.getRight()).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
	}

	public void psetex(Twin<byte[]> kv, long ttlMs){
		try{
			client.psetex(kv.getLeft(), ttlMs, kv.getRight()).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
	}

	public void mset(List<Twin<byte[]>> kvs){
		try{
			client.mset(Scanner.of(kvs).toMap(Twin::getLeft, Twin::getRight)).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
	}

	public void del(byte[] key){
		try{
			client.del(key).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
	}

	public void del(List<byte[]> keys){
		try{
			byte[][] keysArray = keys.toArray(new byte[keys.size()][]);
			client.del(keysArray).get();
		}catch(InterruptedException | ExecutionException e){
			logger.error("", e);
		}
	}

}
