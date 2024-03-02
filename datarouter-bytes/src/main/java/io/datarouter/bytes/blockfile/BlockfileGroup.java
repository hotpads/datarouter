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
package io.datarouter.bytes.blockfile;

import java.util.function.Function;

import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileChecksummers;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressors;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockFormats;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockFormats;
import io.datarouter.bytes.blockfile.io.read.BlockfileReaderBuilder;
import io.datarouter.bytes.blockfile.io.read.metadata.BlockfileMetadataReader;
import io.datarouter.bytes.blockfile.io.read.metadata.BlockfileMetadataReaderBuilder;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriterBuilder;
import io.datarouter.bytes.blockfile.row.BlockfileRow;

public record BlockfileGroup<T>(
		BlockfileStorage storage,
		BlockfileValueBlockFormats registeredValueBlockFormats,
		BlockfileIndexBlockFormats registeredIndexBlockFormats,
		BlockfileCompressors registeredCompressors,
		BlockfileChecksummers registeredChecksummers){

	/*------- writer --------*/

	public BlockfileWriterBuilder<T> newWriterBuilder(
			String name){
		return new BlockfileWriterBuilder<>(this, name);
	}

	/*------- metadata reader --------*/

	public BlockfileMetadataReaderBuilder<T> newMetadataReaderBuilder(
			String pathAndFile){
		return new BlockfileMetadataReaderBuilder<>(this, pathAndFile);
	}

	/*------- reader --------*/

	/**
	 * For cases where you need to obtain something from the header/footer before creating the reader.
	 */
	public BlockfileReaderBuilder<T> newReaderBuilder(
			BlockfileMetadataReader<T> metadataReader,
			Function<BlockfileRow,T> rowDecoder){
		return new BlockfileReaderBuilder<>(this, metadataReader, rowDecoder);
	}

	public BlockfileReaderBuilder<T> newReaderBuilder(
			String pathAndFile,
			Function<BlockfileRow,T> rowDecoder){
		var metadataReader = newMetadataReaderBuilder(pathAndFile)
				.build();
		return new BlockfileReaderBuilder<>(this, metadataReader, rowDecoder);
	}

	public BlockfileReaderBuilder<T> newReaderBuilderKnownFileLength(
			String pathAndFile,
			long knownFileLength,
			Function<BlockfileRow,T> rowDecoder){
		var metadataReader = newMetadataReaderBuilder(pathAndFile)
				.setKnownFileLength(knownFileLength)
				.build();
		return new BlockfileReaderBuilder<>(this, metadataReader, rowDecoder);
	}

}
