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

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.memcached.util.MemcachedResult;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.scanner.Scanner;

public class DatarouterMemcachedClient{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterMemcachedClient.class);

	private final SpyMemcachedClient spyClient;

	public DatarouterMemcachedClient(SpyMemcachedClient spyClient){
		this.spyClient = spyClient;
	}

	public SpyMemcachedClient getSpyClient(){
		return spyClient;
	}

	/*---------- read -------------*/

	public Scanner<MemcachedResult<byte[]>> scanMultiBytes(
			String nodeName,
			Collection<String> keys,
			long timeoutMs,
			boolean ignoreExceptions){
		return scanMulti(nodeName, keys, byte[].class, timeoutMs, ignoreExceptions);
	}

	public Scanner<MemcachedResult<String>> scanMultiStrings(
			String nodeName,
			Collection<String> keys,
			long timeoutMs,
			boolean ignoreExceptions){
		return scanMulti(nodeName, keys, String.class, timeoutMs, ignoreExceptions);
	}

	private <T> Scanner<MemcachedResult<T>> scanMulti(
			String nodeName,
			Collection<String> keys,
			Class<T> castValuesTo,
			long timeoutMs,
			boolean ignoreExceptions){
		if(keys.isEmpty()){
			return Scanner.empty();
		}
		long start = System.currentTimeMillis();
		try(var _ = TracerTool.startSpan(nodeName + " get bulk", TraceSpanGroupType.DATABASE)){
			try{
				Map<String,Object> results = spyClient.asyncGetBulk(keys)
						.get(timeoutMs, TimeUnit.MILLISECONDS);
				TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
						.add("keys", keys.size())
						.add("results", results.size()));
				return Scanner.of(results.entrySet())
						.map(entry -> new MemcachedResult<>(entry.getKey(), castValuesTo.cast(entry.getValue())));
			}catch(TimeoutException e){
				TracerTool.appendToSpanInfo("memcached timeout");
				long elapsedMs = System.currentTimeMillis() - start;
				String details = "timeout after " + elapsedMs + "ms";
				var enhancedException = new RuntimeException(details, e);
				if(ignoreExceptions){
					logger.error("", enhancedException);
					return Scanner.empty();
				}
				throw enhancedException;
			}catch(Exception e){
				TracerTool.appendToSpanInfo("memcached exception");
				String message = String.format("error fetching keys=%s", keys);
				var enhancedException = new RuntimeException(message, e);
				if(ignoreExceptions){
					logger.error("", enhancedException);
					return Scanner.empty();
				}
				throw enhancedException;
			}
		}
	}

	/*---------- write -------------*/

	public void set(
			String nodeName,
			String key,
			int expiration,
			byte[] bytes){
		try(var _ = TracerTool.startSpan(nodeName + " " + "set", TraceSpanGroupType.DATABASE)){
			TracerTool.appendToSpanInfo("bytes", bytes.length);
			try{
				spyClient.set(key, expiration, bytes);
			}catch(Exception e){
				String message = String.format("error incrementing key=%s", key);
				throw new RuntimeException(message, e);
			}
		}
	}

	/*---------- increment -------------*/

	public Long increment(String key, int delta, int expiration, boolean ignoreException){
		// this cannot be async and use the client wide operationTimeout, with default of 2.5s
		try{
			return spyClient.incr(key, delta, delta, expiration);
		}catch(Exception e){
			String message = String.format("error incrementing key=%s", key);
			if(ignoreException){
				logger.warn(message, e);
				return null;
			}else{
				throw new RuntimeException(message, e);
			}
		}
	}

	/*---------- delete -------------*/

	public void delete(
			String nodeName,
			String key,
			Duration timeout){
		try(var _ = TracerTool.startSpan(nodeName + " " + "delete", TraceSpanGroupType.DATABASE)){
			long start = System.currentTimeMillis();
			try{
				spyClient.delete(key)
						.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			}catch(TimeoutException e){
				TracerTool.appendToSpanInfo("memcached timeout");
				long elapsedMs = System.currentTimeMillis() - start;
				String details = "timeout after " + elapsedMs + "ms";
				throw new RuntimeException(details, e);
			}catch(Exception e){
				TracerTool.appendToSpanInfo("memcached exception");
				String message = String.format("error deleting key=%s", key);
				throw new RuntimeException(message, e);
			}
		}
	}

	public void deleteTally(
			String nodeName,
			String key,
			Duration timeout,
			boolean ignoreException){
		try{
			delete(nodeName, key, timeout);
		}catch(Exception exception){
			if(ignoreException){
				logger.error("memcached error on {}", key, exception);
			}else{
				throw exception;
			}
		}
	}

}
