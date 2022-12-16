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

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.scanner.Scanner;

public class KvFileCollatorTests{

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
						VarIntTool.decodeInt(binaryKv.key()),
						ComparableIntCodec.INSTANCE.decode(binaryKv.version()),
						binaryKv.op()));
	}

	private static final KvFileCodec<TestKv> KV_FILE_CODEC = new KvFileCodec<>(TestKv.CODEC);

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

	private static final List<TestKv> EXPECTED_KEEPING_EVERYTHING = List.of(
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

	private static final List<TestKv> EXPECTED_KEEPING_LATEST = List.of(
			new TestKv(0, 4),
			new TestKv(1, 2));

	private static List<TestKv> merge(Function<KvFileCollator,Scanner<KvFileEntry>> mergeMethod){
		KvFileCollator merger = Scanner.of(INPUTS)
				.map(KV_FILE_CODEC::toByteArray)
				.map(ByteArrayInputStream::new)
				.map(KvFileReader::new)
				.listTo(KvFileCollator::new);
		return mergeMethod.apply(merger)
				.map(TestKv.CODEC::decode)
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
		Assert.assertEquals(ALL_INPUTS_SORTED, EXPECTED_KEEPING_EVERYTHING);
	}

	@Test
	private void testKeepingEverything(){
		List<TestKv> actual = merge(KvFileCollator::mergeKeepingEverything);
		Assert.assertEquals(actual, EXPECTED_KEEPING_EVERYTHING);
	}

	@Test
	private void testKeepingLatestVersion(){
		List<TestKv> actual = merge(KvFileCollator::mergeKeepingLatestVersion);
		Assert.assertEquals(actual, EXPECTED_KEEPING_LATEST);
	}

}
