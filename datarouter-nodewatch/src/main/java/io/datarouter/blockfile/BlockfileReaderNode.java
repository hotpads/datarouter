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
package io.datarouter.blockfile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader.BlockfileKeyRange;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.ScannerConfigTool;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.combo.reader.SortedMapStorageReader;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.util.tuple.Range;

public class BlockfileReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements SortedMapStorageReader<PK,D>{

	private final BlockfileDatabeanCodec<PK,D,F> codec;
	private final BlockfileReader<D> blockfileReader;

	public BlockfileReaderNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType){
		super(params, clientType);
		codec = new BlockfileDatabeanCodec<>(getFieldInfo());
		var blockfileNodeParams = params.getBlockfileNodeParams();
		BlockfileGroup<D> group = params.getBlockfileNodeParams().blockfileGroup();
		String filename = blockfileNodeParams.filename();
		blockfileReader = group.newReaderBuilder(filename, codec::decode).build();
	}

	/*----------------------------- MapStorageReader ------------------------*/

	@Override
	public boolean exists(PK key, Config config){
		return findInternal(key).isPresent();
	}

	@Override
	public D get(PK key, Config config){
		return findInternal(key).orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return Scanner.of(keys)
				.concatOpt(this::findInternal)
				.list();
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return Scanner.of(keys)
				.concatOpt(this::findInternal)
				.map(Databean::getKey)
				.list();
	}

	private Optional<D> findInternal(PK key){
		return blockfileReader.rowKey().item(codec.encodeKey(key));
	}

	/*--------------------- SortedStorageReader -----------------------------*/

	@Override
	public Scanner<PK> scanKeys(Range<PK> range, Config config){
		return scanInternal(range, config)
				.map(Databean::getKey);
	}

	@Override
	public Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		return Scanner.of(ranges)
				.concat(range -> scanInternal(range, config))
				.map(Databean::getKey);
	}

	@Override
	public Scanner<D> scan(Range<PK> range, Config config){
		return scanInternal(range, config);
	}

	@Override
	public Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		return Scanner.of(ranges)
				.concat(range -> scanInternal(range, config));
	}

	private Scanner<D> scanInternal(Range<PK> range, Config config){
		var blockfileKeyRange = new BlockfileKeyRange(
				range.findStart().map(codec::encodeKey),
				range.getStartInclusive(),
				range.findEnd().map(codec::encodeKey),
				range.getEndInclusive());
		Scanner<D> scanner = blockfileReader.rowKeyRange().scanRange(blockfileKeyRange);
		return ScannerConfigTool.applyOffsetAndLimit(scanner, config);
	}

}
