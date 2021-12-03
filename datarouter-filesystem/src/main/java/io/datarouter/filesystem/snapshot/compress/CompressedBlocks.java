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
package io.datarouter.filesystem.snapshot.compress;

import java.util.List;

import io.datarouter.bytes.ByteTool;
import io.datarouter.scanner.Scanner;

public class CompressedBlocks{

	public final List<CompressedBlock> blocks;
	public final int count;
	public final int totalLength;

	public CompressedBlocks(List<CompressedBlock> blocks){
		this.blocks = blocks;
		this.count = blocks.size();
		this.totalLength = blocks.stream()
				.mapToInt(block -> block.totalLength)
				.sum();
	}

	public Scanner<byte[]> chunkScanner(){
		return Scanner.of(blocks)
				.map(block -> block.chunks)
				.concat(Scanner::of);
	}

	public byte[] concat(){
		return Scanner.of(blocks)
				.map(block -> block.chunks)
				.concat(Scanner::of)
				.listTo(ByteTool::concatenate);
	}

}
