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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.datarouter.client.memcached.codec.MemcachedDatabeanCodec;
import io.datarouter.client.memcached.codec.MemcachedKey;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.util.HashMethods;
import io.datarouter.web.config.service.ServiceName;

public class MemcachedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D,F>{

	private final MemcachedDatabeanCodec<PK,D,F> codec;
	private final MemcachedBlobNode blobNode;
	private final ServiceName serviceName;
	private final String clientName;
	private final String tableName;
	private final Integer schemaVersion;
	private final Long autoSchemaVersion;

	public MemcachedMapStorageNode(NodeParams<PK,D,F> params, ClientType<?,?> clientType,
			MemcachedBlobNode blobNode, ServiceName serviceName){
		super(params, clientType);
		this.schemaVersion = Optional.ofNullable(params.getSchemaVersion()).orElse(1);
		this.codec = new MemcachedDatabeanCodec<>(
				getName(),
				schemaVersion,
				getFieldInfo().getSampleFielder(),
				getFieldInfo().getDatabeanSupplier(),
				getFieldInfo().getFieldByPrefixedName());
		this.blobNode = blobNode;
		this.serviceName = serviceName;
		this.clientName = getFieldInfo().getClientId().getName();
		this.tableName = getFieldInfo().getTableName();
		this.autoSchemaVersion = createAutoSchemaVersion();
	}

	public Long createAutoSchemaVersion(){
		List<String> fieldNames = new ArrayList<>();
		fieldNames.addAll(getFieldInfo().getNonKeyFieldColumnNames());
		fieldNames.addAll(getFieldInfo().getPrimaryKeyFieldColumnNames());
		String allFieldNamesConcatenated = fieldNames.stream().collect(Collectors.joining("+"));
		return HashMethods.longDjbHash(allFieldNamesConcatenated);
	}

	@Override
	public boolean exists(PK key, Config config){
		return scanMultiInternal(List.of(key))
				.hasAny();
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
				.map(key -> MemcachedKey.encodeKeyToPathbeanKey(serviceName.get(), clientName, tableName,
						schemaVersion, autoSchemaVersion, key))
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
				.map(databean -> codec.encodeDatabeanToPathbeanKeyValueIfValid(databean, serviceName.get(), clientName,
						tableName, autoSchemaVersion))
				.concat(OptionalScanner::of)
				.forEach(keyAndValue -> blobNode.write(keyAndValue.getLeft(), keyAndValue.getRight()));
	}

	private Scanner<D> scanMultiInternal(Collection<PK> keys){
		return Scanner.of(Scanner.of(keys)
				.map(key -> MemcachedKey.encodeKeyToPathbeanKey(serviceName.get(), clientName,
						tableName, schemaVersion, autoSchemaVersion, key))
				.listTo(blobNode::read)
				.values()
				.stream()
				.map(codec::decodeBytes));
	}

}
