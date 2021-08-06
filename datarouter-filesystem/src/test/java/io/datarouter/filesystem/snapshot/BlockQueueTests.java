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
package io.datarouter.filesystem.snapshot;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.filesystem.snapshot.compress.CompressedBlock;
import io.datarouter.filesystem.snapshot.writer.BlockQueue;

public class BlockQueueTests{

	@Test
	public void test1(){
		var queue = new BlockQueue("test", 25, 2);
		int nextBlockId = 0;

		var optResult0 = queue.submit(nextBlockId++, new CompressedBlock(new byte[10]));
		Assert.assertTrue(optResult0.isEmpty());

		var optResult1 = queue.submit(nextBlockId++, new CompressedBlock(new byte[10]));
		var result1 = optResult1.get(0);
		Assert.assertEquals(result1.id, 0);
		Assert.assertEquals(result1.compressedBlocks.count, 2);
		Assert.assertEquals(result1.concat().length, 20);

		var optResult2 = queue.submit(nextBlockId++, new CompressedBlock(new byte[30]));
		var result2 = optResult2.get(0);
		Assert.assertEquals(result2.id, 1);
		Assert.assertEquals(result2.compressedBlocks.count, 1);
		Assert.assertEquals(result2.concat().length, 30);

		var optResult4 = queue.submit(nextBlockId++, new CompressedBlock(new byte[3]));
		Assert.assertTrue(optResult4.isEmpty());

		var optResult5 = queue.takeLastFiles();
		var result5 = optResult5.get(0);
		Assert.assertEquals(result5.id, 2);
		Assert.assertEquals(result5.compressedBlocks.count, 1);
		Assert.assertEquals(result5.concat().length, 3);

		Assert.assertTrue(queue.takeLastFiles().isEmpty());
	}

}
