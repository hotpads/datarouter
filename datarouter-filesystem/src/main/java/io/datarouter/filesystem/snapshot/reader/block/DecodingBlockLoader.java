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
package io.datarouter.filesystem.snapshot.reader.block;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.branch.BranchBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.compress.BlockDecompressor;
import io.datarouter.filesystem.snapshot.encode.BlockDecoder;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.filesystem.snapshot.reader.block.LeafBlockRangeLoader.LeafBlockRange;
import io.datarouter.filesystem.snapshot.storage.block.SnapshotBlockStorageReader;
import io.datarouter.model.util.Bytes;
import io.datarouter.scanner.Scanner;

public class DecodingBlockLoader implements BlockLoader{

	private final SnapshotBlockStorageReader snapshotBlockStorageReader;
	private final SnapshotPaths paths;
	private final BlockDecompressor blockDecompressor;
	private final BlockDecoder blockDecoder;

	public DecodingBlockLoader(
			SnapshotBlockStorageReader snapshotBlockStorageReader,
			SnapshotPaths paths,
			BlockDecompressor blockDecompressor,
			BlockDecoder blockDecoder){
		this.snapshotBlockStorageReader = snapshotBlockStorageReader;
		this.paths = paths;
		this.blockDecompressor = blockDecompressor;
		this.blockDecoder = blockDecoder;
	}

	@Override
	public RootBlock root(BlockKey key){
		//root is not compressed
		byte[] encodedBytes = snapshotBlockStorageReader.getRootBlock();
		return blockDecoder.root(encodedBytes);
	}

	@Override
	public BranchBlock branch(BlockKey key){
		byte[] compressedBytes = snapshotBlockStorageReader.getBranchBlock(paths, key);
		byte[] encodedBytes = blockDecompressor.branch(compressedBytes);
		return blockDecoder.branch(encodedBytes);
	}

	@Override
	public LeafBlock leaf(BlockKey key){
		byte[] compressedBytes = snapshotBlockStorageReader.getLeafBlock(paths, key);
		byte[] encodedBytes = blockDecompressor.leaf(compressedBytes);
		return blockDecoder.leaf(encodedBytes);
	}

	@Override
	public Scanner<LeafBlock> leafRange(LeafBlockRange range){
		BlockKey rangeBlockKey = range.rangeBlockKey();
		byte[] compressedRangeBytes = snapshotBlockStorageReader.getLeafBlock(paths, rangeBlockKey);
		return range.parse(compressedRangeBytes)
				.map(Bytes::toArray)//TODO let decompressors accept Bytes to avoid this mem copy?
				.map(blockDecompressor::leaf)
				.map(blockDecoder::leaf);
	}

	@Override
	public ValueBlock value(BlockKey key){
		byte[] compressedBytes = snapshotBlockStorageReader.getValueBlock(paths, key);
		byte[] encodedBytes = blockDecompressor.value(compressedBytes);
		return blockDecoder.value(encodedBytes);
	}

}
