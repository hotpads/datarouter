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
package io.datarouter.bytes.blockfile.test.row;

import java.util.List;
import java.util.function.BiFunction;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.blockfile.index.BlockfileRangeSearchTool;
import io.datarouter.bytes.blockfile.index.BlockfileRowIdRange;

public class BlockfileRangeSearchToolTests{

	BlockfileRowIdRange block0 = new BlockfileRowIdRange(2, 7);
	BlockfileRowIdRange block1 = new BlockfileRowIdRange(10, 15);
	BlockfileRowIdRange block2 = new BlockfileRowIdRange(20, 29);
	BlockfileRowIdRange block3 = new BlockfileRowIdRange(30, 30);
	List<BlockfileRowIdRange> blocks = List.of(block0, block1, block2, block3);
	BiFunction<BlockfileRowIdRange,Integer,Integer> comp = BlockfileRowIdRange::compareTo;

	@Test
	public void testFindStart(){
		Assert.assertEquals(findStartIndex(0), -1);
		Assert.assertEquals(findStartIndex(3), 0);
		Assert.assertEquals(findStartIndex(8), 1);
		Assert.assertEquals(findStartIndex(10), 1);
		Assert.assertEquals(findStartIndex(15), 1);
		Assert.assertEquals(findStartIndex(16), 2);
		Assert.assertEquals(findStartIndex(23), 2);
		Assert.assertEquals(findStartIndex(29), 2);
		Assert.assertEquals(findStartIndex(30), 3);
		Assert.assertEquals(findStartIndex(35), 4);
	}

	private int findStartIndex(int value){
		return BlockfileRangeSearchTool.startIndex(
				blocks.size(),
				blocks::get,
				item -> item.compareTo(value));
	}

	@Test
	public void testFindEnd(){
		Assert.assertEquals(findEndIndex(0), -1);
		Assert.assertEquals(findEndIndex(3), 0);
		Assert.assertEquals(findEndIndex(8), 0);
		Assert.assertEquals(findEndIndex(10), 1);
		Assert.assertEquals(findEndIndex(15), 1);
		Assert.assertEquals(findEndIndex(16), 1);
		Assert.assertEquals(findEndIndex(23), 2);
		Assert.assertEquals(findEndIndex(29), 2);
		Assert.assertEquals(findEndIndex(30), 3);
		Assert.assertEquals(findEndIndex(34), 3);
	}

	private int findEndIndex(int value){
		return BlockfileRangeSearchTool.endIndex(
				blocks.size(),
				blocks::get,
				item -> item.compareTo(value));
	}

}
