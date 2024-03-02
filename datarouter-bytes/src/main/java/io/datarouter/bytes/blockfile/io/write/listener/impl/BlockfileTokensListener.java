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
package io.datarouter.bytes.blockfile.io.write.listener.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.io.write.listener.BlockfileListener;
import io.datarouter.scanner.Scanner;

public class BlockfileTokensListener implements BlockfileListener{

	private final Consumer<List<BlockfileBaseTokens>> onComplete;
	private final List<BlockfileBaseTokens> blockTokens = new ArrayList<>();

	public BlockfileTokensListener(Consumer<List<BlockfileBaseTokens>> onComplete){
		this.onComplete = onComplete;
	}

	@Override
	public void accept(BlockfileBaseTokens tokens){
		blockTokens.add(tokens);
	}

	public List<BlockfileBaseTokens> blockTokens(){
		return blockTokens;
	}

	public Scanner<BlockfileBaseTokens> scanHeaderAndValueAndIndexTokens(){
		return Scanner.of(blockTokens)
				.include(tokens -> tokens.blockType() == BlockfileBlockType.HEADER
						|| tokens.blockType() == BlockfileBlockType.VALUE
						|| tokens.blockType() == BlockfileBlockType.INDEX);
	}

	public Optional<BlockfileBaseTokens> lastBlockTokens(){
		return blockTokens.isEmpty()
				? Optional.empty()
				: Optional.of(blockTokens.getLast());
	}

	@Override
	public void complete(){
		onComplete.accept(blockTokens);
	}

}
