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
package io.datarouter.bytes.blockfile.read;

import java.util.Optional;

import io.datarouter.bytes.blockfile.Blockfile;
import io.datarouter.bytes.blockfile.read.BlockfileMetadataReader.BlockfileMetadataReaderConfig;
import io.datarouter.bytes.blockfile.section.BlockfileHeader.BlockfileHeaderCodec;

public class BlockfileMetadataReaderBuilder<T>{

	// required
	private final Blockfile<T> blockfile;
	private final String name;
	// optional
	private Optional<Long> knownFileLength = Optional.empty();

	// construct
	public BlockfileMetadataReaderBuilder(Blockfile<T> blockfile, String name){
		this.blockfile = blockfile;
		this.name = name;
	}

	//options
	public BlockfileMetadataReaderBuilder<T> setKnownFileLength(long knownFileLength){
		this.knownFileLength = Optional.of(knownFileLength);
		return this;
	}

	// build
	public BlockfileMetadataReader<T> build(){
		var headerCodec = new BlockfileHeaderCodec(
				blockfile.registeredCompressors(),
				blockfile.registeredChecksummers());
		var config = new BlockfileMetadataReaderConfig<T>(
				blockfile.storage(),
				headerCodec,
				knownFileLength);
		return new BlockfileMetadataReader<>(config, name);
	}

}
