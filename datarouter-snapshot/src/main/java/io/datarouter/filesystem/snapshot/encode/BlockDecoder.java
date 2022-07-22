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

import java.util.List;

import io.datarouter.filesystem.snapshot.block.branch.BranchBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.util.lang.ReflectionTool;

/**
 * Use reflection to create Block instances based on the strings stored in the RootBlock and BlockTypeRegistry.
 */
public class BlockDecoder{

	private final Class<? extends RootBlock> rootBlockClass;
	private final Class<? extends BranchBlock> branchBlockClass;
	private final Class<? extends LeafBlock> leafBlockClass;
	private final Class<? extends ValueBlock> valueBlockClass;

	public BlockDecoder(
			Class<? extends RootBlock> rootBlockClass,
			Class<? extends BranchBlock> branchBlockClass,
			Class<? extends LeafBlock> leafBlockClass,
			Class<? extends ValueBlock> valueBlockClass){
		this.rootBlockClass = rootBlockClass;
		this.branchBlockClass = branchBlockClass;
		this.leafBlockClass = leafBlockClass;
		this.valueBlockClass = valueBlockClass;
	}

	public RootBlock root(byte[] bytes){
		return ReflectionTool.createWithParameters(rootBlockClass, List.of(bytes));
	}

	public BranchBlock branch(byte[] bytes){
		return ReflectionTool.createWithParameters(branchBlockClass, List.of(bytes));
	}

	public LeafBlock leaf(byte[] bytes){
		return ReflectionTool.createWithParameters(leafBlockClass, List.of(bytes));
	}

	public ValueBlock value(byte[] bytes){
		return ReflectionTool.createWithParameters(valueBlockClass, List.of(bytes));
	}

}
