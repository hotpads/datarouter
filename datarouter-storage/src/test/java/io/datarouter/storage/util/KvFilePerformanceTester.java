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
package io.datarouter.storage.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.bytes.kvfile.kv.KvFileOp;
import io.datarouter.bytes.kvfile.read.KvFileCollator.KvFileCollatorPruneDeletesScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;

public class KvFilePerformanceTester{
	private static final Logger logger = LoggerFactory.getLogger(KvFilePerformanceTester.class);

	/*--------- byte[] -------------*/

	private static byte[] makeKey(long slabId, long entryId){
		return ByteTool.concat(
				ComparableLongCodec.INSTANCE.encode(slabId),
				ComparableLongCodec.INSTANCE.encode(entryId));
	}

	private static List<byte[]> makeKeys(long slabId){
		return Scanner.iterate(0L, entryId -> entryId + 1)
				.limit(1_000_000)
				.map(entryId -> makeKey(slabId, entryId))
				.list();
	}

	private static List<List<byte[]>> makeKeyLists(int numLists){
		return Scanner.iterate(0, i -> i + 1)
				.limit(numLists)
				.map(KvFilePerformanceTester::makeKeys)
				.list();
	}

	/*--------- KvFileEntry -------------*/

	private static KvFileEntry makeEntry(long slabId, long entryId){
		return KvFileEntry.create(makeKey(slabId, entryId), EmptyArray.BYTE, KvFileOp.PUT, EmptyArray.BYTE);
	}

	private static List<KvFileEntry> makeList(long slabId){
		return Scanner.iterate(0L, entryId -> entryId + 1)
				.limit(1_000_000)
				.map(entryId -> makeEntry(slabId, entryId))
				.list();
	}

	private static List<List<KvFileEntry>> makeLists(int numLists){
		return Scanner.iterate(0, i -> i + 1)
				.limit(numLists)
				.map(KvFilePerformanceTester::makeList)
				.list();
	}

	/*---------- test performance -------*/

	@Test
	public void testCount(){
		List<List<byte[]>> keyLists = makeKeyLists(20);
		logger.warn("made keys");
		for(int i = 0; i < 10; ++i){
			@SuppressWarnings("unused")
			var counter = new AtomicLong();
			long startMs = System.currentTimeMillis();
			long count = Scanner.of(keyLists)
					.concat(Scanner::of)// 420mm/s
//					.each($ -> counter.incrementAndGet())// 117mm/s
//					.each($ -> counter.incrementAndGet())// 77mm/s
//					.each($ -> counter.incrementAndGet())// 42mm/s
					.count();
			long durationMs = System.currentTimeMillis() - startMs;
			long rps = count * 1000 / durationMs;
			logger.warn("i={}, count={}, durationMs={}, rps={}", i, count, durationMs, NumberFormatter.addCommas(rps));
			System.gc();
		}
	}

	@Test
	public void testDeduplicateBytes(){
		List<List<byte[]>> keyLists = makeKeyLists(20);
		logger.warn("made keys");
		for(int i = 0; i < 10; ++i){
			long startMs = System.currentTimeMillis();
			long count = Scanner.of(keyLists)
					.concat(Scanner::of)
					.deduplicateConsecutiveBy(Function.identity(), Arrays::equals)// 117mm/s
					.count();
			long durationMs = System.currentTimeMillis() - startMs;
			long rps = count * 1000 / durationMs;
			logger.warn("i={}, count={}, durationMs={}, rps={}", i, count, durationMs, NumberFormatter.addCommas(rps));
			System.gc();
		}
	}

	@Test
	public void testCollate(){
		List<List<byte[]>> keyLists = makeKeyLists(20);
		logger.warn("made keys");
		for(int i = 0; i < 10; ++i){
			long startMs = System.currentTimeMillis();
			long count = Scanner.of(keyLists)
					.collateV2(Scanner::of, Arrays::compareUnsigned)// 95mm/s
					.count();
			long durationMs = System.currentTimeMillis() - startMs;
			long rps = count * 1000 / durationMs;
			logger.warn("i={}, count={}, durationMs={}, rps={}", i, count, durationMs, NumberFormatter.addCommas(rps));
			System.gc();
		}
	}

	@Test
	public void testDeduplicateKvs(){
		List<List<KvFileEntry>> lists = makeLists(20);
		logger.warn("made lists");
		for(int i = 0; i < 10; ++i){
			long startMs = System.currentTimeMillis();
			long count = Scanner.of(lists)
					.concat(Scanner::of)
//					.deduplicateConsecutiveBy(Function.identity(), KvFileEntry.EQUALS_KEY)// 90mm/s
					.deduplicateConsecutiveBy(Function.identity(), KvFileEntry::equalsKeyOptimized)// 103mm/s
					.count();
			long durationMs = System.currentTimeMillis() - startMs;
			long rps = count * 1000 / durationMs;
			logger.warn("i={}, count={}, durationMs={}, rps={}", i, count, durationMs, NumberFormatter.addCommas(rps));
			System.gc();
		}
	}

	@Test
	public void testCollateKvs(){
		List<List<KvFileEntry>> lists = makeLists(20);
		logger.warn("made lists");
		for(int i = 0; i < 10; ++i){
			System.gc();
			long startMs = System.currentTimeMillis();
			long count = Scanner.of(lists)
//					.collateV2(Scanner::of, KvFileEntry.COMPARE_KEY_VERSION_OP)// 33mm/s
					.collateV2(Scanner::of, KvFileEntry::compareKeyVersionOpOptimized)// 37mm/s

//					.splitBy(Function.identity(), KvFileEntry.EQUALS_KEY)// 22mm/s
					.splitBy(Function.identity(), KvFileEntry::equalsKeyOptimized)// 26mm/s

					.map(entries -> entries.findLast().orElseThrow())// 19mm/s
					.exclude(kv -> kv.op() == KvFileOp.DELETE)// 17mm/s
					.concat(Scanner::of)
					.count();
			long durationMs = System.currentTimeMillis() - startMs;
			long rps = count * 1000 / durationMs;
			logger.warn("i={}, count={}, durationMs={}, rps={}", i, count, durationMs, NumberFormatter.addCommas(rps));
		}
	}

	@Test
	public void testCollateKvsV2(){
		List<List<KvFileEntry>> lists = makeLists(20);
		logger.warn("made lists");
		for(int i = 0; i < 10; ++i){
			System.gc();
			long startMs = System.currentTimeMillis();
			long count = Scanner.of(lists)
					.collateV2(Scanner::of, KvFileEntry::compareKeyVersionOpOptimized)// 37mm/s
					.link(KvFileCollatorPruneDeletesScanner::new)// 34mm/s
					.concat(Scanner::of)
					.count();
			long durationMs = System.currentTimeMillis() - startMs;
			long rps = count * 1000 / durationMs;
			logger.warn("i={}, count={}, durationMs={}, rps={}", i, count, durationMs, NumberFormatter.addCommas(rps));
		}
	}

}
