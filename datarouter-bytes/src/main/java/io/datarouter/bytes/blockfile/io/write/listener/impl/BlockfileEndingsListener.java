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

/**
 * Note this does not record endings for the footer or trailer since those can be computed separately.
 * They complicate the process of storing endings in the footer.
 */
public class BlockfileEndingsListener implements BlockfileListener{

	private final Consumer<List<Long>> onComplete;
	private final List<Long> endings = new ArrayList<>();

	public BlockfileEndingsListener(Consumer<List<Long>> onComplete){
		this.onComplete = onComplete;
	}

	@Override
	public void accept(BlockfileBaseTokens tokens){
		if(BlockfileBlockType.FOOTER == tokens.blockType()){
			return;
		}
		long length = tokens.totalLength();
		long newEnding = lastEnding().orElse(0L) + length;
		endings.add(newEnding);
	}

	public List<Long> allEndings(){
		return endings;
	}

	public Scanner<Long> scanAllEndings(){
		return Scanner.of(endings);
	}

	public Optional<Long> lastEnding(){
		return endings.isEmpty()
				? Optional.empty()
				: Optional.of(endings.getLast());
	}

	@Override
	public void complete(){
		onComplete.accept(endings);
	}

}
