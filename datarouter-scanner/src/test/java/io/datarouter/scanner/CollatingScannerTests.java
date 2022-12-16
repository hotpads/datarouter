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
package io.datarouter.scanner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CollatingScannerTests{
	private static final Logger logger = LoggerFactory.getLogger(CollatingScannerTests.class);

	@Test
	public void test(){
		Scanner<List<Integer>> batches = Scanner.of(
				List.of(),
				List.of(2, 7, 8),
				List.of(1, 2, 7),
				List.of(),
				List.of(),
				List.of(0, 9));
		List<Integer> actual = batches
				.collate(Scanner::of)
				.list();
		logger.warn("actual={}", actual);
		List<Integer> expected = List.of(0, 1, 2, 2, 7, 7, 8, 9);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testBiggerData(){
		List<Long> input = Scanner.iterate(0L, i -> i + 1)
				.limit(100_000)
				.concatIter(i -> List.of(i, i, i))//create some duplicates
				.list();
		List<Long> randomizedInput = Scanner.of(input)
				.shuffle()
				.list();
		List<List<Long>> sortedInputBatches = Scanner.of(randomizedInput)
				.batch(10_000)
				.map(batch -> Scanner.of(batch).sort().list())
				.list();
		List<Long> output = Scanner.of(sortedInputBatches)
				.collate(Scanner::of)
				.list();
		Assert.assertEquals(output, input);
	}

}
