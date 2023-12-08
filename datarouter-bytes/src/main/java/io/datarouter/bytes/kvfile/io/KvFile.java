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
package io.datarouter.bytes.kvfile.io;

import java.util.List;
import java.util.Optional;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.Blockfile;
import io.datarouter.bytes.kvfile.blockformat.KvFileBlockFormat;
import io.datarouter.bytes.kvfile.blockformat.KvFileBlockFormats;
import io.datarouter.bytes.kvfile.io.read.KvFileMetadataReader;
import io.datarouter.bytes.kvfile.io.read.KvFileMetadataReader.KvFileMetadataReaderConfig;
import io.datarouter.bytes.kvfile.io.read.KvFileReaderBuilder;
import io.datarouter.bytes.kvfile.io.write.KvFileWriterBuilder;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;

public record KvFile<T>(
		Blockfile<List<T>> blockfile,
		KvFileBlockFormats kvBlockFormats){

	/*------ writer -------*/

	public KvFileWriterBuilder<T> newWriterBuilder(
			String pathAndFile,
			Codec<T,KvFileEntry> codec,
			KvFileBlockFormat kvBlockFormat){
		return new KvFileWriterBuilder<>(this, codec, pathAndFile, kvBlockFormat);
	}

	/*------ reader -------*/

	public KvFileMetadataReader<T> newMetadataReader(
			String pathAndFile){
		var blockfileMetadataReader = blockfile.newMetadataReaderBuilder(pathAndFile).build();
		var config = new KvFileMetadataReaderConfig<>(blockfileMetadataReader, kvBlockFormats);
		return new KvFileMetadataReader<>(config);
	}

	//TODO accept metadataReader if already exists
	public KvFileReaderBuilder<T> newReaderBuilder(
			String pathAndFile,
			Codec<T,KvFileEntry> codec){
		return new KvFileReaderBuilder<>(
				this,
				codec,
				pathAndFile,
				Optional.empty());
	}

	//TODO accept metadataReader if already exists
	public KvFileReaderBuilder<T> newReaderBuilderKnownFileLength(
			String pathAndFile,
			long knownFileLength,
			Codec<T,KvFileEntry> codec){
		return new KvFileReaderBuilder<>(
				this,
				codec,
				pathAndFile,
				Optional.of(knownFileLength));
	}

}
