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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class ParallelMappingScannerTests{
	private static final Logger logger = LoggerFactory.getLogger(ParallelMappingScannerTests.class);

	private final ExecutorService executor = Executors.newFixedThreadPool(5);
	private final Function<Integer,Integer> mapper = sleepMs -> {
		trySleep(sleepMs);
		return sleepMs;
	};

	@AfterClass
	public void afterClass(){
		executor.shutdown();
	}

	@Test
	public void testOrdered(){
		List<Integer> expected = List.of(300, 50, 0, 200);
		Scanner<Integer> input = Scanner.of(expected);
		ParallelScannerContext context = new ParallelScannerContext(executor, 5, false);
		List<Integer> actual = input
				.parallel(context)
				.map(mapper)
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testUnordered(){
		Scanner<Integer> input = Scanner.of(300, 50, 0, 200);
		List<Integer> expected = List.of(0, 50, 200, 300);
		ParallelScannerContext context = new ParallelScannerContext(executor, 5, true);
		List<Integer> actual = input
				.parallel(context)
				.map(mapper)
				.list();
		Assert.assertEquals(actual, expected);
	}

	private static void trySleep(long ms){
		if(ms <= 0){//sleep errors on negatives
			return;
		}
		try{
			Thread.sleep(ms);
		}catch(InterruptedException e){
			logger.warn("sleep interrupted, continuing");
		}
	}

}
