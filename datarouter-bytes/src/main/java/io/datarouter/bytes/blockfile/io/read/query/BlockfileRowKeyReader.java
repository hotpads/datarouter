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
package io.datarouter.bytes.blockfile.io.read.query;

import java.util.Optional;

import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockCodec;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileEncodedValueBlock;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.row.BlockfileRow;

public class BlockfileRowKeyReader<T>{

	private final BlockfileReader<T> reader;
	private final BlockfileIndexBlockCodec indexBlockCodec;
	private final BlockfileValueBlockCodec valueBlockCodec;

	public BlockfileRowKeyReader(BlockfileReader<T> reader){
		this.reader = reader;
		indexBlockCodec = reader.metadata().header().indexBlockFormat().supplier().get();
		valueBlockCodec = reader.metadata().header().valueBlockFormat().supplier().get();
	}

	public record BlockfileRowKeySearchResult<T>(
			BlockfileEncodedValueBlock encodedValueBlock,
			long globalBlockId,
			long valueBlockId,
			long firstItemId){
	}

	public Optional<BlockfileRowKeySearchResult<T>> lastValueBlockSpanningRowKey(byte[] key){
		BlockfileIndexBlock indexBlock = reader.metadata().rootIndex();
		while(indexBlock.level() > 0){
			Optional<BlockfileIndexEntry> optIndex = indexBlockCodec.lastChildContainingKey(indexBlock, key);
			if(optIndex.isEmpty()){
				return Optional.empty();
			}
			BlockfileIndexEntry indexEntry = optIndex.orElseThrow();
			indexBlock = reader.loadIndexBlock(indexEntry);
		}
		Optional<BlockfileIndexEntry> optIndex = indexBlockCodec.lastChildContainingKey(indexBlock, key);
		if(optIndex.isEmpty()){
			return Optional.empty();
		}
		BlockfileIndexEntry indexEntry = optIndex.orElseThrow();
		BlockfileEncodedValueBlock encodedValueBlock = reader.loadEncodedValueBlock(indexEntry);
		BlockfileRowKeySearchResult<T> result = new BlockfileRowKeySearchResult<>(
				encodedValueBlock,
				indexEntry.childGlobalBlockId(),
				indexEntry.childIndexOrValueBlockId(),
				indexEntry.rowIdRange().first());
		return Optional.of(result);
	}

	public Optional<BlockfileRowKeySearchResult<T>> valueBlockContainingRowKey(byte[] key){
		return lastValueBlockSpanningRowKey(key)
				.filter(result -> valueBlockCodec.containsKey(result.encodedValueBlock(), key));
	}

	public Optional<BlockfileRow> row(byte[] key){
		return lastValueBlockSpanningRowKey(key)
				.map(BlockfileRowKeySearchResult::encodedValueBlock)
				.flatMap(encodedValueBlock -> valueBlockCodec.findLatestVersion(encodedValueBlock, key));
	}

	public Optional<T> item(byte[] key){
		return row(key)
				.map(reader.config().rowDecoder()::apply);
	}

}
