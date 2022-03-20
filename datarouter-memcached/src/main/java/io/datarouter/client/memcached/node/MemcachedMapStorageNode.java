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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.MemcachedOps;
import io.datarouter.client.memcached.codec.MemcachedDatabeanCodecV2;
import io.datarouter.client.memcached.codec.MemcachedTallyCodecV2;
import io.datarouter.client.memcached.util.MemcachedExpirationTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.config.service.ServiceName;

public class MemcachedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D,F>, PhysicalTallyStorageNode<PK,D,F>{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedMapStorageNode.class);

	public static final int CODEC_VERSION = 1;
	private static final Boolean DEFAULT_IGNORE_EXCEPTION = true;

	private final MemcachedOps ops;
	private final ClientId clientId;
	private final String tableName;
	private final Subpath nodeSubpath;
	private final MemcachedBlobNode blobNode;
	private final MemcachedDatabeanCodecV2<PK,D,F> databeanCodec;
	private final MemcachedTallyCodecV2 tallyCodec;

	public MemcachedMapStorageNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			ServiceName serviceName,
			MemcachedClientManager memcachedClientManager){
		super(params, clientType);
		ops = new MemcachedOps(memcachedClientManager);
		clientId = params.getClientId();
		tableName = getFieldInfo().getTableName();
		String nodeVersion = Optional.ofNullable(params.getSchemaVersion())
				.map(Object::toString)
				.orElse("");
		String databeanVersion = MemcachedDatabeanNodeTool.makeDatabeanVersion(
				getFieldInfo().getFieldColumnNames());
		nodeSubpath = MemcachedDatabeanNodeTool.makeSubpath(
				Integer.toString(CODEC_VERSION),
				serviceName.get(),
				clientId.getName(),
				Integer.toString(1),//placeholder
				tableName,
				nodeVersion,
				databeanVersion);
		NodeParamsBuilder<PK,D,F> blobParamsBuilder = new NodeParamsBuilder<>(params)
				.withPath(nodeSubpath);
		blobNode = new MemcachedBlobNode(
				toPathbeanParams(blobParamsBuilder.build()),
				clientType,
				memcachedClientManager);
		databeanCodec = new MemcachedDatabeanCodecV2<>(
				getFieldInfo().getSampleFielder(),
				getFieldInfo().getDatabeanSupplier(),
				getFieldInfo().getFieldByPrefixedName(),
				nodeSubpath.toString().length());
		tallyCodec = new MemcachedTallyCodecV2(nodeSubpath);
	}

	@SuppressWarnings("unchecked")
	private NodeParams<PathbeanKey,Pathbean,PathbeanFielder> toPathbeanParams(NodeParams<PK,D,F> params){
		return (NodeParams<PathbeanKey,Pathbean,PathbeanFielder>)params;
	}

	/*------------- MapStorage -------------*/

	@Override
	public boolean exists(PK key, Config config){
		return databeanCodec.encodeKey(key)
				.map(blobNode::exists)
				.orElse(false);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return scanMultiInternal(keys)
				.map(Databean::getKey)
				.list();
	}

	@Override
	public D get(PK key, Config config){
		return scanMultiInternal(List.of(key))
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return scanMultiInternal(keys)
				.list();
	}

	@Override
	public void delete(PK key, Config config){
		deleteMulti(List.of(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		Scanner.of(keys)
				.map(databeanCodec::encodeKey)
				.concat(OptionalScanner::of)
				.forEach(blobNode::delete);
	}

	@Override
	public void deleteAll(Config config){
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(D databean, Config config){
		putMulti(List.of(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		Scanner.of(databeans)
				.map(databeanCodec::encodeDatabeanIfValid)
				.concat(OptionalScanner::of)
				.forEach(keyAndValue -> blobNode.write(keyAndValue.getLeft(), keyAndValue.getRight()));
	}

	private Scanner<D> scanMultiInternal(Collection<PK> keys){
		Map<PathbeanKey,byte[]> bytesByKey = Scanner.of(keys)
				.map(databeanCodec::encodeKey)
				.concat(OptionalScanner::of)
				.listTo(blobNode::read);
		return Scanner.of(bytesByKey.values())
				.map(databeanCodec::decodeDatabean);
	}

	/*------------- TallyStorage -------------*/

	@Override
	public Long incrementAndGetCount(String tallyStringKey, int delta, Config config){
		Optional<String> memcachedStringKey = tallyCodec.encodeKey(tallyStringKey);
		if(memcachedStringKey.isEmpty()){
			return 0L;
		}
		int expirationSeconds = MemcachedExpirationTool.getExpirationSeconds(config);
		try{
			return ops.increment(clientId, memcachedStringKey.get(), delta, expirationSeconds);
		}catch(RuntimeException exception){
			if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("memcached error on " + memcachedStringKey, exception);
				return null;
			}
			throw exception;
		}
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		return Optional.ofNullable(getMultiTallyCount(List.of(key), config).get(key));
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> tallyStringKeys, Config config){
		return Scanner.of(tallyStringKeys)
				.map(tallyCodec::encodeKey)
				.concat(OptionalScanner::of)
				.listTo(memcachedStringKeys -> ops.fetch(
						clientId,
						getName(),
						memcachedStringKeys,
						config.getTimeout().toMillis(),
						config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)))
				.map(tallyCodec::decodeResult)
				.toMap(Pair::getLeft, Pair::getRight);
	}

	@Override
	public void deleteTally(String tallyStringKey, Config config){
		tallyCodec.encodeKey(tallyStringKey)
				.ifPresent(memcachedStringKey -> deleteInternal(memcachedStringKey, config));
	}

	private void deleteInternal(String memcachedStringKey, Config config){
		try{
			ops.delete(clientId, getName(), memcachedStringKey, config.getTimeout());
		}catch(Exception exception){
			if(config.ignoreExceptionOrUse(DEFAULT_IGNORE_EXCEPTION)){
				logger.error("memcached error on " + memcachedStringKey, exception);
			}else{
				throw exception;
			}
		}
	}

}
