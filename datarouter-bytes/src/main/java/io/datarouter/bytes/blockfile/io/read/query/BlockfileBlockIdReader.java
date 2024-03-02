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
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileEncodedValueBlock;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;

public class BlockfileBlockIdReader<T>{

	private final BlockfileReader<T> reader;
	private final BlockfileIndexBlockCodec indexBlockCodec;

	public BlockfileBlockIdReader(BlockfileReader<T> reader){
		this.reader = reader;
		indexBlockCodec = reader.metadata().header().indexBlockFormat().supplier().get();
	}

	public record BlockfileBlockIdSearchResult<T>(
			BlockfileEncodedValueBlock encodedValueBlock,
			long globalBlockId,
			long valueBlockId,
			long firstItemId){
	}

	public BlockfileBlockIdSearchResult<T> valueBlockId(long valueBlockId){
		BlockfileIndexBlock indexBlock = reader.metadata().rootIndex();
		while(indexBlock.level() > 0){
			BlockfileIndexEntry indexEntry = indexBlockCodec.childContainingValueBlockId(indexBlock, valueBlockId);
			indexBlock = reader.loadIndexBlock(indexEntry);
		}
		BlockfileIndexEntry indexEntry = indexBlockCodec.childContainingValueBlockId(indexBlock, valueBlockId);
		BlockfileEncodedValueBlock encodedValueBlock = reader.loadEncodedValueBlock(indexEntry);
		return new BlockfileBlockIdSearchResult<>(
				encodedValueBlock,
				indexEntry.childGlobalBlockId(),
				indexEntry.childIndexOrValueBlockId(),
				indexEntry.rowIdRange().first());
	}

}
