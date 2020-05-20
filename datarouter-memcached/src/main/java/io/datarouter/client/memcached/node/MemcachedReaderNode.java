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
package io.datarouter.client.memcached.node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.MemcachedEncodedKey;
import io.datarouter.instrumentation.trace.TraceSpanFinisher;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.TallyStorageReader;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.TallyKey;

public class MemcachedReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>, TallyStorageReader<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedReaderNode.class);

	protected static final Boolean DEFAULT_IGNORE_EXCEPTION = true;

	protected final Integer databeanVersion;
	protected final MemcachedClientManager memcachedClientManager;
	protected final ClientId clientId;

	public MemcachedReaderNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			MemcachedClientManager memcachedClientManager,
			ClientId clientId){
		super(params, clientType);
		this.memcachedClientManager = memcachedClientManager;
		this.clientId = clientId;
		this.databeanVersion = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
	}

	@Override
	public boolean exists(PK key, Config config){
		if(key == null){
			return false;
		}
		return get(key, config) != null;
	}

	@Override
	public D get(PK key, Config config){
		if(key == null){
			return null;
		}
		return getMulti(List.of(key), config).stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config params){
		if(keys == null || keys.isEmpty()){
			return List.of();
		}
		return Scanner.of(getMulti(keys, params)).map(Databean::getKey).list();
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return List.of();
		}
		Map<String,Object> bytesByStringKey = fetchBytesByStringKey(keys, config);
		if(bytesByStringKey == null){ // an ignored error occurred
			return List.of();
		}

		List<D> databeans = new ArrayList<>(keys.size());
		for(Entry<String,Object> entry : bytesByStringKey.entrySet()){
			byte[] bytes = (byte[])entry.getValue();
			if(bytes.length == 0){
				throw new RuntimeException("empty memcached response key=" + entry.getKey());
			}
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			try{
				D databean = FieldSetTool.fieldSetFromByteStreamKnownLength(
						getFieldInfo().getDatabeanSupplier(),
						getFieldInfo().getFieldByPrefixedName(),
						is,
						bytes.length);
				databeans.add(databean);
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}
		return databeans;
	}

	protected Map<String,Object> fetchBytesByStringKey(Collection<? extends PrimaryKey<?>> keys, Config config){
		List<String> memcachedKeys = buildMemcachedKeys(keys);
		long start = 0;
		try(var $ = startTraceSpan("get bulk")){
			try{
				start = System.currentTimeMillis();
				Map<String,Object> results = memcachedClientManager.getSpyMemcachedClient(clientId)
						.asyncGetBulk(memcachedKeys)
						.get(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
				TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder()
						.add("keys", memcachedKeys.size())
						.add("results", results.size()));
				return results;
			}catch(TimeoutException e){
				TracerTool.appendToSpanInfo("memcached timeout");
				String details = "timeout after " + (System.currentTimeMillis() - start) + "ms";
				if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
					logger.error(details, e);
					return null;
				}
				throw new RuntimeException(details, e);
			}catch(ExecutionException | InterruptedException e){
				TracerTool.appendToSpanInfo("memcached exception");
				if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
					logger.error("", e);
					return null;
				}
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		if(key == null){
			return Optional.empty();
		}
		return Optional.ofNullable(getMultiTallyCount(List.of(key), config).get(key));
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return Collections.emptyMap();
		}
		Map<String,Object> bytesByStringKey = Scanner.of(keys)
				.map(TallyKey::new)
				.listTo(tallyKeys -> fetchBytesByStringKey(tallyKeys, config));
		if(bytesByStringKey == null){ // an ignored error occurred
			return Collections.emptyMap();
		}

		Map<String,Long> results = new HashMap<>();
		for(Entry<String,Object> entry : bytesByStringKey.entrySet()){
			String string = (String)entry.getValue();
			MemcachedEncodedKey memcachedKey = MemcachedEncodedKey.parse(entry.getKey(), TallyKey.class);
			results.put(((TallyKey)memcachedKey.primaryKey).getId(), Long.parseLong(string));
		}
		return results;
	}

	protected String buildMemcachedKey(PrimaryKey<?> pk){
		return buildMemcachedKeys(List.of(pk)).get(0);
	}

	protected List<String> buildMemcachedKeys(Collection<? extends PrimaryKey<?>> pks){
		return MemcachedEncodedKey.getVersionedKeyStrings(getName(), databeanVersion, pks);
	}

	protected TraceSpanFinisher startTraceSpan(String opName){
		return TracerTool.startSpan(getName() + " " + opName);
	}

}
