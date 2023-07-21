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
package io.datarouter.bytes.kvfile;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.bytes.kvfile.kv.KvFileOp;
import io.datarouter.bytes.kvfile.read.KvFileCollator;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.scanner.Scanner;

public class KvFileCollatorTests{
	private static final Logger logger = LoggerFactory.getLogger(KvFileCollatorTests.class);

	private record TestKv(
			int key,
			int version,
			KvFileOp op){

		public TestKv(
				int key,
				int version){
			this(key, version, KvFileOp.PUT);
		}

		static final Comparator<TestKv> COMPARE = Comparator.comparing(TestKv::key)
				.thenComparing(TestKv::version)
				.thenComparing(TestKv::op);

		static final Codec<TestKv,KvFileEntry> CODEC = Codec.of(
				testKv -> KvFileEntry.create(
						VarIntTool.encode(testKv.key),
						ComparableIntCodec.INSTANCE.encode(testKv.version),
						testKv.op,
						EmptyArray.BYTE),
				binaryKv -> new TestKv(
						VarIntTool.decodeInt(binaryKv.copyOfKey()),
						ComparableIntCodec.INSTANCE.decode(binaryKv.copyOfVersion()),
						binaryKv.op()));
	}

	private static final List<TestKv> INPUT_0 = List.of(
			new TestKv(0, 0),
			new TestKv(0, 2),
			new TestKv(0, 2),//duplicate
			new TestKv(0, 3, KvFileOp.DELETE),
			new TestKv(0, 3, KvFileOp.DELETE),//duplicate delete
			new TestKv(1, 0),
			new TestKv(1, 1),
			new TestKv(2, 0));
	private static final List<TestKv> INPUT_1 = List.of(
			new TestKv(0, 1),
			new TestKv(1, 0, KvFileOp.DELETE),//same version as the next put and should be ignored
			new TestKv(1, 0),//sorts after the above delete and should be kept
			new TestKv(1, 2),
			new TestKv(2, 1));
	private static final List<TestKv> INPUT_2 = List.of(
			new TestKv(0, 4),
			new TestKv(2, 2, KvFileOp.DELETE),
			new TestKv(2, 3),
			new TestKv(2, 4, KvFileOp.DELETE));//true delete

	private static final List<List<TestKv>> INPUTS = List.of(INPUT_0, INPUT_1, INPUT_2);
	private static final List<TestKv> ALL_INPUTS_SORTED = Scanner.of(INPUTS)
			.concat(Scanner::of)
			.sort(TestKv.COMPARE)
			.list();

	private static final List<TestKv> EXPECTED_KEEP_ALL = List.of(
			new TestKv(0, 0),
			new TestKv(0, 1),
			new TestKv(0, 2),
			new TestKv(0, 2),
			new TestKv(0, 3, KvFileOp.DELETE),
			new TestKv(0, 3, KvFileOp.DELETE),
			new TestKv(0, 4),
			new TestKv(1, 0, KvFileOp.DELETE),
			new TestKv(1, 0),
			new TestKv(1, 0),
			new TestKv(1, 1),
			new TestKv(1, 2),
			new TestKv(2, 0),
			new TestKv(2, 1),
			new TestKv(2, 2, KvFileOp.DELETE),
			new TestKv(2, 3),
			new TestKv(2, 4, KvFileOp.DELETE));

	private static final List<TestKv> EXPECTED_PRUNE_VERSIONS = List.of(
			new TestKv(0, 4),
			new TestKv(1, 2),
			new TestKv(2, 4, KvFileOp.DELETE));

	private static final List<TestKv> EXPECTED_PRUNE_ALL = List.of(
			new TestKv(0, 4),
			new TestKv(1, 2));

	private static List<TestKv> collate(Function<List<Scanner<KvFileEntry>>,Scanner<KvFileEntry>> collateMethod){
		return Scanner.of(INPUTS)
				.map(inputGroup -> Scanner.of(inputGroup).map(TestKv.CODEC::encode))
				.listTo(collateMethod)
				.map(TestKv.CODEC::decode)
				.each(kv -> logger.info("kv={}", kv))
				.list();
	}

	@Test
	private void testEachInputListIsSorted(){
		INPUTS.forEach(input -> {
			List<TestKv> sorted = Scanner.of(input)
					.sort(TestKv.COMPARE)
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
		List<TestKv> actual = collate(KvFileCollator::keepAll);
		Assert.assertEquals(actual, EXPECTED_KEEP_ALL);
	}

	@Test
	private void testPruneVersions(){
		List<TestKv> actual = collate(KvFileCollator::pruneVersions);
		Assert.assertEquals(actual, EXPECTED_PRUNE_VERSIONS);
	}

	@Test
	private void testPruneAll(){
		List<TestKv> actual = collate(KvFileCollator::pruneAll);
		Assert.assertEquals(actual, EXPECTED_PRUNE_ALL);
	}

}
