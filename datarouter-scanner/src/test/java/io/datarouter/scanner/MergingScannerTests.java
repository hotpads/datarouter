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

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MergingScannerTests{
	private static final Logger logger = LoggerFactory.getLogger(MergingScannerTests.class);

	@Test
	public void test(){
		int numThreads = 2;
		var exec = Executors.newFixedThreadPool(numThreads);
		var threads = new Threads(exec, numThreads);
		Comparator<Integer> comparator = Comparator.nullsLast(Comparator.naturalOrder());

		List<Integer> concatenated = makeInputScanners()
				.concat(Function.identity())
				.list();
		logger.warn("concatenated={}", concatenated);

		List<Integer> merged = makeInputScanners()
				.merge(threads, Function.identity())
				.list();
		logger.warn("merged={}", merged);

		Assert.assertNotEquals(concatenated, merged);
		Assert.assertEquals(
				Scanner.of(concatenated).sort(comparator).list(),
				Scanner.of(merged).sort(comparator).list());

		exec.shutdown();
	}

	private Scanner<Scanner<Integer>> makeInputScanners(){
		Scanner<Integer> evens = makeInputScanner(0, 2, 10);
		Scanner<Integer> odds = makeInputScanner(1, 2, 30);
		return Scanner.of(evens, odds);
	}

	private Scanner<Integer> makeInputScanner(int offset, int stride, long pauseMs){
		return Scanner.iterate(offset, i -> i + stride)
				.each($ -> pause(pauseMs))
				.limit(5)
				.append((Integer)null);
	}

	private static void pause(long ms){
		try{
			Thread.sleep(ms);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}

}
