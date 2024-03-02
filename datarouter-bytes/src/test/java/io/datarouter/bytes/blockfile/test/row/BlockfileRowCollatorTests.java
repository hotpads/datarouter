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

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.blockfile.row.BlockfileRowCollator;
import io.datarouter.bytes.blockfile.row.BlockfileRowOp;
import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.scanner.Scanner;

public class BlockfileRowCollatorTests{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileRowCollatorTests.class);

	private record TestRow(
			int key,
			int version,
			BlockfileRowOp op){

		public TestRow(
				int key,
				int version){
			this(key, version, BlockfileRowOp.PUT);
		}

		static final Comparator<TestRow> COMPARE = Comparator.comparing(TestRow::key)
				.thenComparing(TestRow::version)
				.thenComparing(TestRow::op);

		static final Codec<TestRow,BlockfileRow> CODEC = Codec.of(
				testRow -> BlockfileRow.create(
						VarIntTool.encode(testRow.key),
						ComparableIntCodec.INSTANCE.encode(testRow.version),
						testRow.op,
						EmptyArray.BYTE),
				blockfileRow -> new TestRow(
						VarIntTool.decodeInt(blockfileRow.copyOfKey()),
						ComparableIntCodec.INSTANCE.decode(blockfileRow.copyOfVersion()),
						blockfileRow.op()));
	}

	private static final List<TestRow> INPUT_0 = List.of(
			new TestRow(0, 0),
			new TestRow(0, 2),
			new TestRow(0, 2),//duplicate
			new TestRow(0, 3, BlockfileRowOp.DELETE),
			new TestRow(0, 3, BlockfileRowOp.DELETE),//duplicate delete
			new TestRow(1, 0),
			new TestRow(1, 1),
			new TestRow(2, 0));
	private static final List<TestRow> INPUT_1 = List.of(
			new TestRow(0, 1),
			new TestRow(1, 0, BlockfileRowOp.DELETE),//same version as the next put and should be ignored
			new TestRow(1, 0),//sorts after the above delete and should be kept
			new TestRow(1, 2),
			new TestRow(2, 1));
	private static final List<TestRow> INPUT_2 = List.of(
			new TestRow(0, 4),
			new TestRow(2, 2, BlockfileRowOp.DELETE),
			new TestRow(2, 3),
			new TestRow(2, 4, BlockfileRowOp.DELETE));//true delete

	private static final List<List<TestRow>> INPUTS = List.of(INPUT_0, INPUT_1, INPUT_2);
	private static final List<TestRow> ALL_INPUTS_SORTED = Scanner.of(INPUTS)
			.concat(Scanner::of)
			.sort(TestRow.COMPARE)
			.list();

	private static final List<TestRow> EXPECTED_KEEP_ALL = List.of(
			new TestRow(0, 0),
			new TestRow(0, 1),
			new TestRow(0, 2),
			new TestRow(0, 2),
			new TestRow(0, 3, BlockfileRowOp.DELETE),
			new TestRow(0, 3, BlockfileRowOp.DELETE),
			new TestRow(0, 4),
			new TestRow(1, 0, BlockfileRowOp.DELETE),
			new TestRow(1, 0),
			new TestRow(1, 0),
			new TestRow(1, 1),
			new TestRow(1, 2),
			new TestRow(2, 0),
			new TestRow(2, 1),
			new TestRow(2, 2, BlockfileRowOp.DELETE),
			new TestRow(2, 3),
			new TestRow(2, 4, BlockfileRowOp.DELETE));

	private static final List<TestRow> EXPECTED_PRUNE_VERSIONS = List.of(
			new TestRow(0, 4),
			new TestRow(1, 2),
			new TestRow(2, 4, BlockfileRowOp.DELETE));

	private static final List<TestRow> EXPECTED_PRUNE_ALL = List.of(
			new TestRow(0, 4),
			new TestRow(1, 2));

	private static List<TestRow> collate(Function<List<Scanner<BlockfileRow>>,Scanner<BlockfileRow>> collateMethod){
		return Scanner.of(INPUTS)
				.map(inputGroup -> Scanner.of(inputGroup)
						.map(TestRow.CODEC::encode))
				.listTo(collateMethod)
				.map(TestRow.CODEC::decode)
				.each(kv -> logger.info("kv={}", kv))
				.list();
	}

	@Test
	private void testEachInputListIsSorted(){
		INPUTS.forEach(input -> {
			List<TestRow> sorted = Scanner.of(input)
					.sort(TestRow.COMPARE)
					.list();
			Assert.assertEquals(sorted, input);
		});
	}

	@Test
	private void testExpectedAllMatchesSortedInputs(){
		Assert.assertEquals(ALL_INPUTS_SORTED, EXPECTED_KEEP_ALL);
	}

	@Test
	private void testKeepAll(){
		List<TestRow> actual = collate(BlockfileRowCollator::keepAll);
		Assert.assertEquals(actual, EXPECTED_KEEP_ALL);
	}

	@Test
	private void testPruneVersions(){
		List<TestRow> actual = collate(BlockfileRowCollator::pruneVersions);
		Assert.assertEquals(actual, EXPECTED_PRUNE_VERSIONS);
	}

	@Test
	private void testPruneAll(){
		List<TestRow> actual = collate(BlockfileRowCollator::pruneAll);
		Assert.assertEquals(actual, EXPECTED_PRUNE_ALL);
	}

}
