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
package io.datarouter.bytes.blockfile.io.write;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileIndexTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileValueTokens;
import io.datarouter.bytes.blockfile.index.BlockfileByteRange;
import io.datarouter.bytes.blockfile.io.storage.BlockfileLocation;

public class BlockfileWriterState{

	private final AtomicLong cursor = new AtomicLong();
	private final AtomicLong numItems = new AtomicLong();
	private final AtomicLong numGlobalBlocks = new AtomicLong();
	private final AtomicLong numValueBlocks = new AtomicLong();
	private final AtomicLong numIndexBlocks = new AtomicLong();
	private final AtomicInteger headerBlockLength = new AtomicInteger();
	private final AtomicReference<BlockfileLocation> latestIndexBlockLocation = new AtomicReference<>();
	private final AtomicInteger footerBlockLength = new AtomicInteger();

	/*--------- append blocks ---------*/

	public BlockfileByteRange appendHeaderBlock(BlockfileBaseTokens tokens){
		takeGlobalBlockId();
		headerBlockLength.set(tokens.totalLength());
		return appendBlock(tokens);
	}

	public BlockfileByteRange appendValueBlock(BlockfileValueTokens tokens){
		takeGlobalBlockId();
		BlockfileByteRange bytesBoundary = appendBlock(tokens);
		return bytesBoundary;
	}

	public BlockfileByteRange appendIndexBlock(BlockfileIndexTokens tokens){
		takeGlobalBlockId();
		latestIndexBlockLocation.set(new BlockfileLocation(cursor.get(), tokens.totalLength()));
		return appendBlock(tokens);
	}

	public BlockfileByteRange appendFooterBlock(BlockfileBaseTokens tokens){
		takeGlobalBlockId();
		footerBlockLength.set(tokens.totalLength());
		return appendBlock(tokens);
	}

	private BlockfileByteRange appendBlock(BlockfileBaseTokens tokens){
		int length = tokens.totalLength();
		long cursorBefore = cursor.get();
		long cursorAfter = cursor.addAndGet(length);
		var boundary = new BlockfileByteRange(cursorBefore, cursorAfter);
		return boundary;
	}

	/*-------- numItems ----------*/

	public long getNumItemsAndAdd(long add){
		return numItems.getAndAdd(add);
	}

	/*-------- numBlocks ----------*/

	public long takeGlobalBlockId(){
		return numGlobalBlocks.getAndIncrement();
	}

	public long previousGlobalBlockId(){
		return numGlobalBlocks.get() - 1;
	}

	public long nextGlobalBlockId(){
		return numGlobalBlocks.get();
	}

	/*-------- header block ---------*/

	public BlockfileLocation headerBlockLocation(){
		return new BlockfileLocation(0, headerBlockLength.get());
	}

	/*-------- value blocks ----------*/

	public long numValueBlocks(){
		return numValueBlocks.get();
	}

	public long takeValueBlockId(){
		return numValueBlocks.getAndIncrement();
	}

	/*-------- index blocks ----------*/

	public long numIndexBlocks(){
		return numIndexBlocks.get();
	}

	public long takeIndexBlockId(){
		return numIndexBlocks.getAndIncrement();
	}

	public BlockfileLocation latestIndexBlockLocation(){
		return latestIndexBlockLocation.get();
	}

	/*---------- footer block ----------*/

	public void setFooterBlockLength(int length){
		footerBlockLength.set(length);
	}

	public int footerBlockLength(){
		return footerBlockLength.get();
	}

	/*--------- file length -----------*/

	public long cursor(){
		return cursor.get();
	}

}
