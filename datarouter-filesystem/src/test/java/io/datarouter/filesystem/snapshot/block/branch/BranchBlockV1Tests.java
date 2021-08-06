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
package io.datarouter.filesystem.snapshot.block.branch;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.bytes.ByteTool;

public class BranchBlockV1Tests{
	private static final Logger logger = LoggerFactory.getLogger(BranchBlockV1Tests.class);

	@Test
	public void testSingleLevel(){
		List<String> inputs = List.of("a", "bb", "ccc");
		var encoder = new BranchBlockV1Encoder(0);
		var keyId = new AtomicLong();
		var childBlockId = new AtomicInteger();//generate fake blockIds
		Scanner.of(inputs)
				.map(String::getBytes)
				.map(bytes -> new SnapshotEntry(bytes, ByteTool.EMPTY_ARRAY, ByteTool.EMPTY_ARRAY_2))
				.forEach(entry -> encoder.add(0, keyId.getAndIncrement(), entry, childBlockId.getAndIncrement()));
		var fileIdsAndEndings = new FileIdsAndEndings(
				new int[]{0, 1, 2, 3},
				new int[]{0, 1, 2, 3});//one for previous block ending, plus one for each key
		byte[] bytes = encoder.encode(fileIdsAndEndings).concat();
		var block = new BranchBlockV1(bytes);
		logger.warn(block.toDetailedString());

		//keyIds
		for(int i = 0; i < inputs.size(); ++i){
			Assert.assertEquals(block.recordId(i), i);
		}

		//keys
		List<String> output = block.keyCopies()
				.map(String::new)
				.list();
		Assert.assertEquals(output, inputs);

		//childBlocks
		for(int i = 0; i < inputs.size(); ++i){
			byte[] searchKey = inputs.get(i).getBytes(StandardCharsets.UTF_8);
			Assert.assertEquals(block.findChildBlockIndex(searchKey), i);
		}
		Assert.assertEquals(block.findChildBlockIndex("Z".getBytes(StandardCharsets.UTF_8)), 0);
		Assert.assertEquals(block.findChildBlockIndex("az".getBytes(StandardCharsets.UTF_8)), 1);
		Assert.assertEquals(block.findChildBlockIndex("baa".getBytes(StandardCharsets.UTF_8)), 1);
		Assert.assertEquals(block.findChildBlockIndex("cadillac".getBytes(StandardCharsets.UTF_8)), 2);
	}

}
