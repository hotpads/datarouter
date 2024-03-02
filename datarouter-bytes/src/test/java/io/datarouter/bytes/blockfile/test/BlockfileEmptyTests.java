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
package io.datarouter.bytes.blockfile.test;

import java.util.List;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.storage.impl.BlockfileLocalStorage;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Scanner;

public class BlockfileEmptyTests{

	/*-------- static ---------*/

	private static final BlockfileStorage STORAGE = new BlockfileLocalStorage("/tmp/datarouterTest/blockfile/empty/");
	private static final String BLOCKFILE_NAME = "block";
	private static final int BLOCK_SIZE = 10;
	private static final int NUM_VALUE_BLOCKS = 0;

	private static final List<BlockfileRow> ROWS = List.of();

	/*---------- fields -----------*/

	private final BlockfileGroup<BlockfileRow> blockfileGroup = new BlockfileGroupBuilder<BlockfileRow>(STORAGE)
			.build();
	private final BlockfileWriter<BlockfileRow> writer = blockfileGroup.newWriterBuilder(BLOCKFILE_NAME)
			.build();
	private final BlockfileReader<BlockfileRow> reader = blockfileGroup.newReaderBuilder(
			BLOCKFILE_NAME,
			Function.identity())
			.build();

	@BeforeClass
	private void beforeClass(){
		Scanner.of(ROWS)
				.batch(BLOCK_SIZE)
				.apply(writer::writeBlocks);
	}

	@Test
	private void testNumValueBlocks(){
		Assert.assertEquals(reader.metadata().footer().numValueBlocks(), NUM_VALUE_BLOCKS);
		Assert.assertEquals(reader.sequential().scanDecodedBlocks().count(), NUM_VALUE_BLOCKS);
	}

	@Test
	private void testScan(){
		List<BlockfileRow> decoded = reader.sequential().scan()
				.list();
		Assert.assertEquals(decoded, ROWS);
	}
}
