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

import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.tuple.Pair;

public class MemcachedOps{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedOps.class);

	private final MemcachedClientManager memcachedClientManager;

	public MemcachedOps(MemcachedClientManager memcachedClientManager){
		this.memcachedClientManager = memcachedClientManager;
	}

	public void delete(
			ClientId clientId,
			String nodeName,
			String memcachedStringKey,
			Duration timeout){
		try(var $ = TracerTool.startSpan(nodeName + " " + "delete", TraceSpanGroupType.DATABASE)){
			long start = System.currentTimeMillis();
			try{
				memcachedClientManager.getSpyMemcachedClient(clientId)
						.delete(memcachedStringKey)
						.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			}catch(TimeoutException e){
				TracerTool.appendToSpanInfo("memcached timeout");
				long elapsedMs = System.currentTimeMillis() - start;
				String details = "timeout after " + elapsedMs + "ms";
				throw new RuntimeException(details, e);
			}catch(Exception e){
				TracerTool.appendToSpanInfo("memcached exception");
				String message = String.format("error deleting key=%s", memcachedStringKey);
				throw new RuntimeException(message, e);
			}
		}
	}
	public Scanner<Pair<String,Object>> fetch(
			ClientId clientId,
			String nodeName,
			Collection<String> memcachedStringKeys,
			long timeoutMs,
			boolean ignoreExceptions){
		if(memcachedStringKeys.isEmpty()){
			return Scanner.empty();
		}
		long start = System.currentTimeMillis();
		try(var $ = TracerTool.startSpan(nodeName + " get bulk", TraceSpanGroupType.DATABASE)){
			try{
				Map<String,Object> results = memcachedClientManager.getSpyMemcachedClient(clientId)
						.asyncGetBulk(memcachedStringKeys)
						.get(timeoutMs, TimeUnit.MILLISECONDS);
				TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
						.add("keys", memcachedStringKeys.size())
						.add("results", results.size()));
				return Scanner.of(results.entrySet())
						.map(entry -> new Pair<>(entry.getKey(), entry.getValue()));
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
				String message = String.format("error fetching keys=%s", memcachedStringKeys);
				var enhancedException = new RuntimeException(message, e);
				if(ignoreExceptions){
					logger.error("", enhancedException);
					return Scanner.empty();
				}
				throw enhancedException;
			}
		}
	}

	public long increment(ClientId clientId, String memcachedStringKey, int delta, int expiration){
		// this cannot be async and use the client wide operationTimeout, with default of 2.5s
		try{
			return memcachedClientManager
					.getSpyMemcachedClient(clientId)
					.incr(memcachedStringKey, delta, delta, expiration);
		}catch(Exception e){
			String message = String.format("error incrementing key=%s", memcachedStringKey);
			throw new RuntimeException(message, e);
		}
	}

	public void set(
			ClientId clientId,
			String nodeName,
			String memcachedStringKey,
			int expiration,
			byte[] bytes){
		try(var $ = TracerTool.startSpan(nodeName + " " + "set", TraceSpanGroupType.DATABASE)){
			TracerTool.appendToSpanInfo("bytes", bytes.length);
			try{
				memcachedClientManager
						.getSpyMemcachedClient(clientId)
						.set(memcachedStringKey, expiration, bytes);
			}catch(Exception e){
				String message = String.format("error incrementing key=%s", memcachedStringKey);
				throw new RuntimeException(message, e);
			}
		}
	}

}