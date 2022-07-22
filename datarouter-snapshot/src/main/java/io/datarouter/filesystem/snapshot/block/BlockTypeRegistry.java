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
package io.datarouter.filesystem.snapshot.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import io.datarouter.filesystem.snapshot.block.branch.BranchBlock;
import io.datarouter.filesystem.snapshot.block.branch.BranchBlockV1;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlockV1;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlockV1;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlockV1;
import io.datarouter.util.Require;
import io.datarouter.util.lang.ReflectionTool;

/**
 * Mapping of persistent block name string to the Block implementation class.  Custom implementations can be added
 * as long as the names are unique.
 */
@Singleton
public class BlockTypeRegistry{

	private final Map<String,Class<? extends RootBlock>> rootClassByName;
	private final Map<String,Class<? extends BranchBlock>> branchClassByName;
	private final Map<String,Class<? extends LeafBlock>> leafClassByName;
	private final Map<String,Class<? extends ValueBlock>> valueClassByName;

	public BlockTypeRegistry(){
		this.rootClassByName = new HashMap<>();
		this.branchClassByName = new HashMap<>();
		this.leafClassByName = new HashMap<>();
		this.valueClassByName = new HashMap<>();

		//register built-in types
		registerRoot(RootBlockV1.FORMAT, RootBlockV1.class);
		registerBranch(BranchBlockV1.FORMAT, BranchBlockV1.class);
		registerLeaf(LeafBlockV1.FORMAT, LeafBlockV1.class);
		registerValue(ValueBlockV1.FORMAT, ValueBlockV1.class);
	}

	/*------------ register --------------*/

	public BlockTypeRegistry registerRoot(String name, Class<? extends RootBlock> blockClass){
		Require.notContains(rootClassByName.keySet(), name);
		rootClassByName.put(name, blockClass);
		return this;
	}

	public BlockTypeRegistry registerBranch(String name, Class<? extends BranchBlock> blockClass){
		Require.notContains(branchClassByName.keySet(), name);
		branchClassByName.put(name, blockClass);
		return this;
	}

	public BlockTypeRegistry registerLeaf(String name, Class<? extends LeafBlock> blockClass){
		Require.notContains(leafClassByName.keySet(), name);
		leafClassByName.put(name, blockClass);
		return this;
	}

	public BlockTypeRegistry registerValue(String name, Class<? extends ValueBlock> blockClass){
		Require.notContains(valueClassByName.keySet(), name);
		valueClassByName.put(name, blockClass);
		return this;
	}

	/*------------------ get ----------------*/

	public Class<? extends RootBlock> getRootClass(String name){
		return rootClassByName.get(name);
	}

	public Class<? extends BranchBlock> getBranchClass(String name){
		return branchClassByName.get(name);
	}

	public Class<? extends LeafBlock> getLeafClass(String name){
		return leafClassByName.get(name);
	}

	public Class<? extends ValueBlock> getValueClass(String name){
		return valueClassByName.get(name);
	}

	/*-------------- decode ----------------*/

	public RootBlock decodeRoot(String typeName, byte[] bytes){
		return ReflectionTool.createWithParameters(getRootClass(typeName), List.of(bytes));
	}

	public BranchBlock decodeBranch(String typeName, byte[] bytes){
		return ReflectionTool.createWithParameters(getBranchClass(typeName), List.of(bytes));
	}

	public LeafBlock decodeLeaf(String typeName, byte[] bytes){
		return ReflectionTool.createWithParameters(getLeafClass(typeName), List.of(bytes));
	}

	public ValueBlock decodeValue(String typeName, byte[] bytes){
		return ReflectionTool.createWithParameters(getValueClass(typeName), List.of(bytes));
	}

}
