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

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.blockfile.BlockfileBuilder;
import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressor;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;

public class KvFileBuilder<T>{

	private final BlockfileBuilder<List<T>> blockfileBuilder;
	//TODO init with built-in formats
	private final List<String> kvBlockFormats = new ArrayList<>();

	public KvFileBuilder(BlockfileStorage blockfileStorage){
		blockfileBuilder = new BlockfileBuilder<>(blockfileStorage);
	}

	/*-------- BlockfileBuilder pass-through options --------*/

	public KvFileBuilder<T> registerChecksummer(BlockfileChecksummer checksummer){
		blockfileBuilder.registerChecksummer(checksummer);
		return this;
	}

	public KvFileBuilder<T> registerCompressor(BlockfileCompressor compressor){
		blockfileBuilder.registerCompressor(compressor);
		return this;
	}

	/*--------- KvFileBuilder options --------*/

	public KvFileBuilder<T> registerKvBlockFormat(String kvBlockFormat){
		kvBlockFormats.add(kvBlockFormat);
		return this;
	}

	/*-------- build -------*/

	public KvFile<T> build(){
		var blockfile = blockfileBuilder.build();
		return new KvFile<>(blockfile, kvBlockFormats);
	}

}
