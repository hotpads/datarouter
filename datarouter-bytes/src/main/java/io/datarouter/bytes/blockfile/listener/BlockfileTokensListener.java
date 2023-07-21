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
package io.datarouter.bytes.blockfile.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.datarouter.bytes.blockfile.dto.BlockfileTokens;
import io.datarouter.bytes.blockfile.enums.BlockfileSection;
import io.datarouter.bytes.blockfile.write.BlockfileListener;
import io.datarouter.scanner.Scanner;

public class BlockfileTokensListener implements BlockfileListener{

	private final Consumer<List<BlockfileTokens>> onComplete;
	private final List<BlockfileTokens> blockTokens = new ArrayList<>();

	public BlockfileTokensListener(Consumer<List<BlockfileTokens>> onComplete){
		this.onComplete = onComplete;
	}

	@Override
	public void accept(BlockfileTokens tokens){
		blockTokens.add(tokens);
	}

	public List<BlockfileTokens> blockTokens(){
		return blockTokens;
	}

	public Scanner<BlockfileTokens> scanHeaderAndBlockTokens(){
		return Scanner.of(blockTokens)
				.include(tokens -> tokens.section() == BlockfileSection.HEADER
						|| tokens.section() == BlockfileSection.BLOCK);
	}

	public Optional<BlockfileTokens> lastBlockTokens(){
		return blockTokens.isEmpty()
				? Optional.empty()
				: Optional.of(blockTokens.get(blockTokens.size() - 1));
	}

	@Override
	public void complete(){
		onComplete.accept(blockTokens);
	}

}
