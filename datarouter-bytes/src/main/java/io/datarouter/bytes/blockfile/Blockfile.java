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

import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummers;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressors;
import io.datarouter.bytes.blockfile.read.BlockfileMetadataReader;
import io.datarouter.bytes.blockfile.read.BlockfileMetadataReaderBuilder;
import io.datarouter.bytes.blockfile.read.BlockfileReaderBuilder;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.write.BlockfileWriterBuilder;

public record Blockfile<T>(
		BlockfileStorage storage,
		BlockfileCompressors registeredCompressors,
		BlockfileChecksummers registeredChecksummers){

	/*------- writer --------*/

	public BlockfileWriterBuilder<T> newWriterBuilder(
			String name,
			Function<T,byte[]> encoder){
		return new BlockfileWriterBuilder<>(this, encoder, name);
	}

	/*------- metadata reader --------*/

	public BlockfileMetadataReaderBuilder<T> newMetadataReaderBuilder(
			String pathAndFile){
		return new BlockfileMetadataReaderBuilder<>(this, pathAndFile);
	}

	/*------- reader --------*/

	public BlockfileReaderBuilder<T> newReaderBuilder(
			BlockfileMetadataReader<T> metadataReader,
			Function<byte[],T> decoder){
		return new BlockfileReaderBuilder<>(this, metadataReader, decoder);
	}

	public BlockfileReaderBuilder<T> newReaderBuilderKnownFileLength(
			String pathAndFile,
			long knownFileLength,
			Function<byte[],T> decoder){
		var metadataReader = newMetadataReaderBuilder(pathAndFile)
				.setKnownFileLength(knownFileLength)
				.build();
		return new BlockfileReaderBuilder<>(this, metadataReader, decoder);
	}

}
