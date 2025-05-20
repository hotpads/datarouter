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

import java.util.List;

import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.scanner.Scanner;

public class BlockfileSequentialReader<T>{

	private final BlockfileReader<T> reader;

	public BlockfileSequentialReader(BlockfileReader<T> reader){
		this.reader = reader;
	}

	public Scanner<T> scan(){
		return reader.sequentialSingleUse(reader.makeInputStream()).scan();
	}

	public Scanner<List<T>> scanDecodedValues(){
		return reader.sequentialSingleUse(reader.makeInputStream()).scanDecodedValues();
	}

	public Scanner<BlockfileDecodedBlock<T>> scanDecodedBlocks(){
		return reader.sequentialSingleUse(reader.makeInputStream()).scanDecodedBlocks();
	}

	public Scanner<BlockfileDecodedBlockBatch<T>> scanDecodedBlockBatches(){
		return reader.sequentialSingleUse(reader.makeInputStream()).scanDecodedBlockBatches();
	}

}
