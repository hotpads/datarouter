/**
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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import io.datarouter.util.Require;
import io.datarouter.util.lang.ReflectionTool;

@Singleton
public class BlockCompressorRegistry{

	private final Map<String,Class<? extends BlockCompressor>> blockCompressorByName;

	public BlockCompressorRegistry(){
		this.blockCompressorByName = new HashMap<>();

		//register built-in types
		register(PassthroughBlockCompressor.NAME, PassthroughBlockCompressor.class);
		register(ChecksumBlockCompressor.NAME, ChecksumBlockCompressor.class);
		register(GzipBlockCompressor.NAME, GzipBlockCompressor.class);
	}

	public BlockCompressorRegistry register(String name, Class<? extends BlockCompressor> blockCompressorClass){
		Require.notContains(blockCompressorByName.keySet(), name);
		blockCompressorByName.put(name, blockCompressorClass);
		return this;
	}

	public Class<? extends BlockCompressor> getClass(String name){
		return blockCompressorByName.get(name);
	}

	public BlockCompressor create(String name){
		Class<? extends BlockCompressor> blockCompressorClass = getClass(name);
		return ReflectionTool.create(blockCompressorClass);
	}

}
