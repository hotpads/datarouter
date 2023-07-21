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
package io.datarouter.bytes.kvfile.io.read;

import java.util.List;

import io.datarouter.bytes.blockfile.read.BlockfileReader;
import io.datarouter.bytes.blockfile.read.BlockfileReader.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.read.BlockfileReader.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.section.BlockfileFooter;
import io.datarouter.bytes.kvfile.io.footer.KvFileFooter;
import io.datarouter.bytes.kvfile.io.header.KvFileHeader;
import io.datarouter.scanner.Scanner;

public class KvFileReader<T>{

	public record KvFileReaderConfig<T>(
			BlockfileReader<List<T>> blockfileReader,
			KvFileMetadataReader<T> kvFileMetadataReader){
	}

	private final KvFileReaderConfig<T> config;

	public KvFileReader(KvFileReaderConfig<T> config){
		this.config = config;
	}

	/*------ header ----------*/

	public KvFileHeader header(){
		return config.kvFileMetadataReader().header();
	}

	/*------ block batches --------*/

	public Scanner<BlockfileDecodedBlockBatch<List<T>>> scanBlockfileDecodedBlockBatches(){
		return config.blockfileReader().scanDecodedBlockBatches();
	}

	public Scanner<List<List<T>>> scanBlockBatches(){
		return scanBlockfileDecodedBlockBatches()
				.map(BlockfileDecodedBlockBatch::values);
	}

	/*------ blocks --------*/

	public Scanner<BlockfileDecodedBlock<List<T>>> scanBlockfileDecodedBlocks(){
		return config.blockfileReader().scanDecodedBlocks();
	}

	public Scanner<List<T>> scanBlocks(){
		return scanBlockfileDecodedBlocks()
				.map(BlockfileDecodedBlock::value);
	}

	/*------ kvs --------*/

	public Scanner<T> scan(){
		return scanBlocks()
				.concat(Scanner::of);
	}

	/*------ footer --------*/

	public BlockfileFooter blockfileFooter(){
		return config.blockfileReader().footer();
	}

	public KvFileFooter footer(){
		return config.kvFileMetadataReader().footer();
	}

}