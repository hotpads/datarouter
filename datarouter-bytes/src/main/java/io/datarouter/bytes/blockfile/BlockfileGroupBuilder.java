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

import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileChecksummers;
import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileStandardChecksummers;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressors;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockFormat;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockFormats;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileStandardIndexBlockFormats;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileStandardValueBlockFormats;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockFormat;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockFormats;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;

public class BlockfileGroupBuilder<T>{

	// required
	private final BlockfileStorage storage;
	// included
	private final BlockfileValueBlockFormats registeredValueBlockFormats = new BlockfileValueBlockFormats(
			BlockfileStandardValueBlockFormats.ALL);
	private final BlockfileIndexBlockFormats registeredIndexBlockFormats = new BlockfileIndexBlockFormats(
			BlockfileStandardIndexBlockFormats.ALL);
	private final BlockfileCompressors registeredCompressors = new BlockfileCompressors(
			BlockfileStandardCompressors.ALL);
	private final BlockfileChecksummers registeredChecksummers = new BlockfileChecksummers(
			BlockfileStandardChecksummers.ALL);

	// construct
	public BlockfileGroupBuilder(BlockfileStorage storage){
		this.storage = storage;
	}

	// options
	public BlockfileGroupBuilder<T> registerBlockFormat(BlockfileValueBlockFormat format){
		registeredValueBlockFormats.add(format);
		return this;
	}

	public BlockfileGroupBuilder<T> registerIndexBlockFormat(BlockfileIndexBlockFormat format){
		registeredIndexBlockFormats.add(format);
		return this;
	}

	public BlockfileGroupBuilder<T> registerCompressor(BlockfileCompressor compressor){
		registeredCompressors.add(compressor);
		return this;
	}

	public BlockfileGroupBuilder<T> registerChecksummer(BlockfileChecksummer checksummer){
		registeredChecksummers.add(checksummer);
		return this;
	}

	// build
	public BlockfileGroup<T> build(){
		return new BlockfileGroup<>(
				storage,
				registeredValueBlockFormats,
				registeredIndexBlockFormats,
				registeredCompressors,
				registeredChecksummers);
	}

}