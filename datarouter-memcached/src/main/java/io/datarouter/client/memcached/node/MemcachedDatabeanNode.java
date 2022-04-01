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
import java.util.function.Supplier;

import io.datarouter.client.memcached.client.DatarouterMemcachedClient;
import io.datarouter.client.memcached.codec.MemcachedDatabeanCodec;
import io.datarouter.client.memcached.util.MemcachedDatabeanNodeTool;
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
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;
import io.datarouter.web.config.service.ServiceName;

public class MemcachedDatabeanNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D,F>{

	public static final int CODEC_VERSION = 1;

	private final ClientId clientId;
	private final MemcachedBlobNode blobNode;
	private final MemcachedDatabeanCodec<PK,D,F> databeanCodec;

	public MemcachedDatabeanNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			ServiceName serviceName,
			Supplier<DatarouterMemcachedClient> lazyClient){
		super(params, clientType);
		clientId = params.getClientId();
		String nodeVersion = Optional.ofNullable(params.getSchemaVersion())
				.map(Object::toString)
				.orElse("");
		String databeanVersion = MemcachedDatabeanNodeTool.makeDatabeanVersion(
				getFieldInfo().getFieldColumnNames());
		Subpath nodeSubpath = MemcachedDatabeanNodeTool.makeSubpath(
				Integer.toString(CODEC_VERSION),
				serviceName.get(),
				clientId.getName(),
				Integer.toString(1),//placeholder
				getFieldInfo().getTableName(),
				nodeVersion,
				databeanVersion);
		NodeParamsBuilder<PK,D,F> blobParamsBuilder = new NodeParamsBuilder<>(params)
				.withPath(nodeSubpath);
		blobNode = new MemcachedBlobNode(
				toPathbeanParams(blobParamsBuilder.build()),
				clientType,
				lazyClient);
		databeanCodec = new MemcachedDatabeanCodec<>(
				getFieldInfo().getSampleFielder(),
				getFieldInfo().getDatabeanSupplier(),
				getFieldInfo().getFieldByPrefixedName(),
				nodeSubpath.toString().length());
	}

	@SuppressWarnings("unchecked")
	private NodeParams<PathbeanKey,Pathbean,PathbeanFielder> toPathbeanParams(NodeParams<PK,D,F> params){
		return (NodeParams<PathbeanKey,Pathbean,PathbeanFielder>)params;
	}

	/*------------- MapStorage -------------*/

	@Override
	public boolean exists(PK key, Config config){
		return databeanCodec.encodeKeyIfValid(key)
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
				.map(databeanCodec::encodeKeyIfValid)
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
				.map(databeanCodec::encodeKeyIfValid)
				.concat(OptionalScanner::of)
				.listTo(blobNode::read);
		return Scanner.of(bytesByKey.values())
				.map(databeanCodec::decodeDatabean);
	}

}
