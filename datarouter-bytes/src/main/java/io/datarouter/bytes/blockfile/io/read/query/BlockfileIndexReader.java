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
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.index.BlockfileRowIdRange;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.Scanner;

public class BlockfileIndexReader<T>{

	private final BlockfileReader<T> reader;
	private final BlockfileIndexBlockCodec indexBlockCodec;

	public BlockfileIndexReader(BlockfileReader<T> reader){
		this.reader = reader;
		indexBlockCodec = reader.metadata().header().indexBlockFormat().supplier().get();
	}

	/*------ root -------*/

	public Scanner<BlockfileIndexEntry> scanRootIndexEntries(){
		return indexBlockCodec.scanChildren(reader.metadata().rootIndex());
	}

	public long numRows(){
		//TODO get last entry without scanning?
		return scanRootIndexEntries()
				.findLast()
				.map(BlockfileIndexEntry::rowIdRange)
				.map(BlockfileRowIdRange::last)
				.map(i -> i + 1)
				.orElse(0L);
	}

	/*------ leaf -------*/

	public Scanner<BlockfileIndexEntry> scanLeafIndexEntries(){
		return indexBlockCodec.scanChildren(reader.metadata().rootIndex())
				.concat(this::scanIndexEntryAndDescendants)
				.include(indexEntry -> indexEntry.level() == 0);
	}

	/*------ all -------*/

	public Scanner<BlockfileIndexEntry> scanIndexEntries(){
		return scanRootIndexEntries()
				.concat(this::scanIndexEntryAndDescendants);
	}

	private Scanner<BlockfileIndexEntry> scanIndexEntryAndDescendants(BlockfileIndexEntry indexEntry){
		Scanner<BlockfileIndexEntry> parent = ObjectScanner.of(indexEntry);
		if(indexEntry.level() == 0){
			return parent;
		}
		BlockfileIndexBlock childIndexBlock = reader.loadIndexBlock(indexEntry);
		Scanner<BlockfileIndexEntry> descendants = indexBlockCodec.scanChildren(childIndexBlock)
				.concat(this::scanIndexEntryAndDescendants);
		return Scanner.concat(parent, descendants);
	}

}
