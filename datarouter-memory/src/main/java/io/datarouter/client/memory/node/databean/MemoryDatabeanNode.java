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
package io.datarouter.client.memory.node.databean;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.client.memory.MemoryClientType;
import io.datarouter.client.memory.util.MemoryDatabeanCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.util.tuple.Range;

public class MemoryDatabeanNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalSortedMapStorageNode<PK,D,F>{

	private final MemoryDatabeanStorage storage;
	private final MemoryDatabeanKeyCodec<PK> keyCodec;
	private final MemoryDatabeanCodec<PK,D,F> databeanCodec;

	public MemoryDatabeanNode(
			NodeParams<PK,D,F> params,
			MemoryClientType memoryClientType){
		super(params, memoryClientType);
		storage = new MemoryDatabeanStorage();
		keyCodec = new MemoryDatabeanKeyCodec<>(
				getFieldInfo().getPrimaryKeySupplier(),
				getFieldInfo().getPrimaryKeyFields());
		databeanCodec = new MemoryDatabeanCodec<>(getFieldInfo());
	}

	/*----------------------- StorageWriter ----------------------*/

	@Override
	public void put(D databean, Config config){
		putMulti(List.of(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		Long ttlMs = config.findTtl()
				.map(Duration::toMillis)
				.orElse(null);
		Scanner.of(databeans)
				.map(this::makeDatabeanAndIndexEntries)
				.flush(binaryKvs -> storage.put(binaryKvs, ttlMs));
	}

	/*----------------------- MapStorageWriter ----------------------*/

	@Override
	public void delete(PK key, Config config){
		deleteMulti(List.of(key), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		Scanner.of(keys)
				.map(keyCodec::encode)
				.flush(storage::delete);
	}

	@Override
	public void deleteAll(Config config){
		storage.deleteAll();
	}

	/*----------------------- MapStorageReader ----------------------*/

	@Override
	public boolean exists(PK key, Config config){
		return Optional.of(key)
				.map(keyCodec::encode)
				.flatMap(storage::find)
				.isPresent();
	}

	@Override
	public D get(PK key, Config config){
		return Optional.of(key)
				.map(keyCodec::encode)
				.flatMap(storage::find)
				.map(MemoryDatabean::getDatabean)
				.map(databeanCodec::decode)
				.orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return Scanner.of(keys)
				.map(keyCodec::encode)
				.listTo(storage::scanMulti)
				.map(MemoryDatabean::getDatabean)
				.map(databeanCodec::decode)
				.list();
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return Scanner.of(keys)
				.map(keyCodec::encode)
				.listTo(storage::scanMulti)
				.map(MemoryDatabean::getKey)
				.map(keyCodec::decode)
				.list();
	}

	/*--------------------- SortedStorageReader ---------------------*/

	@Override
	public Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		return scanInternal(ranges, config)
				.map(MemoryDatabean::getDatabean)
				.map(databeanCodec::decode);
	}

	@Override
	public Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		return scanInternal(ranges, config)
				.map(MemoryDatabean::getKey)
				.map(keyCodec::decode);
	}

	private Scanner<MemoryDatabean> scanInternal(Collection<Range<PK>> ranges, Config config){
		Scanner<MemoryDatabean> scanner = Scanner.of(ranges)
				.map(keyCodec::encodeRange)
				.concat(storage::scan);
		scanner = config.findOffset()
				.map(scanner::skip)
				.orElse(scanner);
		scanner = config.findLimit()
				.map(scanner::limit)
				.orElse(scanner);
		return scanner;
	}

	/*------------- private --------------*/

	private MemoryDatabeanAndIndexEntries makeDatabeanAndIndexEntries(D databean){
		byte[] keyBytes = keyCodec.encode(databean.getKey());
		byte[] databeanBytes = databeanCodec.encode(databean);
		Map<String,byte[]> indexEntries = Map.of();//TODO generate real IndexEntries
		return new MemoryDatabeanAndIndexEntries(keyBytes, databeanBytes, indexEntries);
	}

}
