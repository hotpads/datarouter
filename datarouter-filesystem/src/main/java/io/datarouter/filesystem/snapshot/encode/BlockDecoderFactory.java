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
package io.datarouter.filesystem.snapshot.encode;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.filesystem.snapshot.block.BlockTypeRegistry;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;

@Singleton
public class BlockDecoderFactory{

	@Inject
	private BlockTypeRegistry blockTypeRegistry;

	public BlockDecoder create(RootBlock rootBlock){
		return new BlockDecoder(
				rootBlock.getClass(),
				blockTypeRegistry.getBranchClass(rootBlock.branchBlockType()),
				blockTypeRegistry.getLeafClass(rootBlock.leafBlockType()),
				blockTypeRegistry.getValueClass(rootBlock.valueBlockType()));
	}

}
