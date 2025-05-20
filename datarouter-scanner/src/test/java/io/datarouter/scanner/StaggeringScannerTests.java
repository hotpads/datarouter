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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StaggeringScannerTests{

	private static final long SLOW_TEST_TOLERANCE_MILLIS = 100;
	@Test
	public void testEmpty(){
		Instant startTime = Instant.now();
		List<Integer> output = Scanner.<Integer>empty()
				.stagger(Duration.ofSeconds(10))
				.list();
		Assert.assertEquals(output, List.of());
		Duration duration = Duration.between(startTime, Instant.now());
		// Validate the scanner returns immediately when empty, with some wiggle room for a slow test suite.
		Assert.assertTrue(
				duration.toMillis() <= SLOW_TEST_TOLERANCE_MILLIS,
				String.format("durationMillis=%s", duration.toMillis()));
	}

	@Test
	public void testSingleItem(){
		Instant startTime = Instant.now();
		List<Integer> output = Scanner.of(List.of(777))
				.stagger(Duration.ofSeconds(10))
				.list();
		Assert.assertEquals(output, List.of(777));
		Duration duration = Duration.between(startTime, Instant.now());
		// Validate the scanner returns the first item immediately, with some wiggle room for a slow test suite.
		Assert.assertTrue(
				duration.toMillis() <= SLOW_TEST_TOLERANCE_MILLIS,
				String.format("durationMillis=%s", duration.toMillis()));
	}

	@Test
	public void testMultiItem(){
		int numItems = 10;
		int millisPerItem = 200;
		long desiredDurationMillis = numItems * millisPerItem;
		List<Integer> inputs = IntStream.range(0, numItems).boxed().toList();

		Instant startTime = Instant.now();
		List<Integer> outputs = Scanner.of(inputs)
				.stagger(Duration.ofMillis(desiredDurationMillis))
				.list();
		Assert.assertEquals(outputs, inputs);
		Duration duration = Duration.between(startTime, Instant.now());

		long shortestValidDurationMillis = (numItems - 1) * millisPerItem - 1;
		Assert.assertTrue(
				duration.toMillis() >= shortestValidDurationMillis,
				String.format("durationMillis=%s", duration.toMillis()));

		long longestValidDurationMillis = desiredDurationMillis + SLOW_TEST_TOLERANCE_MILLIS;
		Assert.assertTrue(
				duration.toMillis() <= longestValidDurationMillis,
				String.format("durationMillis=%s", duration.toMillis()));
	}

}
