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
package io.datarouter.storage.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;

public class DatabeanToBlobNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalMapStorageNode<PK,D,F>{

	private final PhysicalBlobStorageNode blobNode;
	private final DatabeanToBlobCodec<PK,D,F> codec;

	public DatabeanToBlobNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			PhysicalBlobStorageNode blobNode,
			DatabeanToBlobCodec<PK,D,F> codec){
		super(params, clientType);
		this.blobNode = blobNode;
		this.codec = codec;
	}

	/*------------- MapStorageReader -------------*/

	@Override
	public boolean exists(PK key, Config config){
		return codec.encodeKeyIfValid(key)
				.map(encodedKey -> blobNode.exists(encodedKey, config))
				.orElse(false);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return scanMultiInternal(keys, config)
				.map(Databean::getKey)
				.list();
	}

	@Override
	public D get(PK key, Config config){
		return scanMultiInternal(List.of(key), config)
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return scanMultiInternal(keys, config)
				.list();
	}

	/*------------- MapStorageWriter -------------*/

	@Override
	public void delete(PK key, Config config){
		deleteMulti(List.of(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		Scanner.of(keys)
				.concatOpt(codec::encodeKeyIfValid)
				.forEach(encodedKey -> blobNode.delete(encodedKey, config));
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
				.concatOpt(codec::encodeDatabeanIfValid)
				.forEach(keyAndValue -> blobNode.write(keyAndValue.pathbeanKey(), keyAndValue.value(), config));
	}

	private Scanner<D> scanMultiInternal(Collection<PK> keys, Config config){
		Map<PathbeanKey,byte[]> bytesByKey = Scanner.of(keys)
				.concatOpt(codec::encodeKeyIfValid)
				.listTo(encodedKeys -> blobNode.readMulti(encodedKeys, config));
		return Scanner.of(bytesByKey.values())
				.map(codec::decodeDatabean);
	}

}
