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

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.util.DatarouterCounters;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

public class DatarouterRedisClient{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterRedisClient.class);

	private final RedisClientType clientType;
	private final ClientId clientId;
	private final RedisClusterAsyncCommands<byte[],byte[]> lettuceClient;

	public DatarouterRedisClient(
			RedisClientType clientType,
			ClientId clientId,
			RedisClusterAsyncCommands<byte[],byte[]> lettuceClient){
		this.clientType = clientType;
		this.clientId = clientId;
		this.lettuceClient = lettuceClient;
	}

	public RedisClusterAsyncCommands<byte[],byte[]> getLettuceClient(){
		return lettuceClient;
	}

	/*------------- read ---------------*/

	public boolean exists(byte[] key, RedisRequestConfig config){
		long numFound = exec("exists", () -> lettuceClient.exists(key), config, 0L);
		return numFound == 1;
	}

	public Optional<byte[]> find(byte[] key, RedisRequestConfig config){
		byte[] value = exec("get", () -> lettuceClient.get(key), config, null);
		return Optional.ofNullable(value);
	}

	public Scanner<KeyValue<byte[],byte[]>> mget(List<byte[]> keys, RedisRequestConfig config){
		byte[][] keysArray = keys.toArray(new byte[keys.size()][]);
		List<KeyValue<byte[],byte[]>> values = exec("mget", () -> lettuceClient.mget(keysArray), config, List.of());
		return Scanner.of(values);
	}

	/*------------- write ---------------*/

	public void set(RedisKeyValue kv, RedisRequestConfig config){
		exec("set", () -> lettuceClient.set(kv.key(), kv.value()), config, null);
	}

	public Long incrby(byte[] key, int by, RedisRequestConfig config){
		Long newValue = exec("incrby", () -> lettuceClient.incrby(key, by), config, null);
		return newValue;
	}

	public void pexpire(byte[] key, Duration ttl, RedisRequestConfig config){
		exec("pexpire", () -> lettuceClient.pexpire(key, ttl.toMillis()), config, null);
	}

	public void psetex(RedisKeyValue kv, long ttlMs, RedisRequestConfig config){
		exec("psetex", () -> lettuceClient.psetex(kv.key(), ttlMs, kv.value()), config, null);
	}

	public void del(byte[] key, RedisRequestConfig config){
		exec("del", () -> lettuceClient.del(key), config, null);
	}

	public void del(byte[][] keys, RedisRequestConfig config){
		exec("del", () -> lettuceClient.del(keys), config, null);
	}

	/*-------------- private ---------------*/

	private <T> T exec(
			String operationName,
			Supplier<RedisFuture<T>> operation,
			RedisRequestConfig config,
			T responseOnFailure){
		DatarouterCounters.incClient(clientType, operationName, clientId.getName(), 1);
		DatarouterCounters.incClientNodeCustom(clientType, operationName, clientId.getName(), config.caller, 1);

		String traceName = String.format("redis %s %s", clientId.getName(), operationName);
		try(var $ = TracerTool.startSpan(traceName, TraceSpanGroupType.DATABASE)){
			RedisFuture<T> future = operation.get();
			Exception exception;
			try{
				return future.get(config.timeout.toMillis(), TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){
				exception = e;
				TracerTool.appendToSpanInfo(e.getClass().getSimpleName());
				Thread.currentThread().interrupt();
			}catch(ExecutionException e){
				exception = e;
				TracerTool.appendToSpanInfo(e.getClass().getSimpleName());
			}catch(TimeoutException e){
				exception = e;
				String spanInfo = String.format("timeout after %sms", config.timeout.toMillis());
				TracerTool.appendToSpanInfo(spanInfo);
			}

			String message = String.format("%s exception=%s operation=%s timeoutMs=%s ignored=%s caller=%s",
					getClass().getSimpleName(),
					exception.getClass().getSimpleName(),
					operationName,
					config.timeout.toMillis(),
					config.ignoreException,
					config.caller);
			if(config.ignoreException){
				if(logger.isDebugEnabled()){
					logger.warn(message, exception);
				}else{
					logger.warn(message);
				}
				return responseOnFailure;
			}
			throw new RuntimeException(message, exception);
		}
	}

}
