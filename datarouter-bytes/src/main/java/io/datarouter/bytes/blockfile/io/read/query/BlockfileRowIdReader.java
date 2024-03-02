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

import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockCodec;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileEncodedValueBlock;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.row.BlockfileRow;

public class BlockfileRowIdReader<T>{

	private final BlockfileReader<T> reader;
	private final BlockfileIndexBlockCodec indexBlockCodec;
	private final BlockfileValueBlockCodec valueBlockCodec;

	public BlockfileRowIdReader(BlockfileReader<T> reader){
		this.reader = reader;
		indexBlockCodec = reader.metadata().header().indexBlockFormat().supplier().get();
		valueBlockCodec = reader.metadata().header().valueBlockFormat().supplier().get();
	}

	public record BlockfileRowIdSearchResult<T>(
			BlockfileEncodedValueBlock encodedValueBlock,
			long globalBlockId,
			long valueBlockId,
			long firstItemId){
	}

	public BlockfileRowIdSearchResult<T> valueBlockWithRowId(long rowId){
		BlockfileIndexBlock indexBlock = reader.metadata().rootIndex();
		while(indexBlock.level() > 0){
			BlockfileIndexEntry indexEntry = indexBlockCodec.childContainingRowId(indexBlock, rowId);
			indexBlock = reader.loadIndexBlock(indexEntry);
		}
		BlockfileIndexEntry indexEntry = indexBlockCodec.childContainingRowId(indexBlock, rowId);
		return new BlockfileRowIdSearchResult<>(
				reader.loadEncodedValueBlock(indexEntry),
				indexEntry.childGlobalBlockId(),
				indexEntry.childIndexOrValueBlockId(),
				indexEntry.rowIdRange().first());
	}

	public BlockfileRow row(long rowId){
		BlockfileRowIdSearchResult<T> blockSearchResult = valueBlockWithRowId(rowId);
		BlockfileEncodedValueBlock encodedValueBlock = blockSearchResult.encodedValueBlock();
		return valueBlockCodec.row(encodedValueBlock, rowId);
	}

	public T item(long rowId){
		BlockfileRow row = row(rowId);
		return reader.config().rowDecoder().apply(row);
	}

}
