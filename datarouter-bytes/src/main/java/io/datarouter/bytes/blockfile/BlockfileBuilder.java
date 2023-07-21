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

import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummers;
import io.datarouter.bytes.blockfile.checksum.BlockfileStandardChecksummers;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressor;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressors;
import io.datarouter.bytes.blockfile.compress.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;

public class BlockfileBuilder<T>{

	// required
	private final BlockfileStorage storage;
	// included
	private final BlockfileCompressors registeredCompressors = new BlockfileCompressors(
			BlockfileStandardCompressors.ALL);
	private final BlockfileChecksummers registeredChecksummers = new BlockfileChecksummers(
			BlockfileStandardChecksummers.ALL);

	// construct
	public BlockfileBuilder(BlockfileStorage storage){
		this.storage = storage;
	}

	// options
	public BlockfileBuilder<T> registerCompressor(BlockfileCompressor compressor){
		registeredCompressors.add(compressor);
		return this;
	}

	public BlockfileBuilder<T> registerChecksummer(BlockfileChecksummer checksummer){
		registeredChecksummers.add(checksummer);
		return this;
	}

	// build
	public Blockfile<T> build(){
		return new Blockfile<>(
				storage,
				registeredCompressors,
				registeredChecksummers);
	}

}