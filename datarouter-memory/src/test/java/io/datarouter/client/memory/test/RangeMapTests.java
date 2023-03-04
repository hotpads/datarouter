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
package io.datarouter.client.memory.test;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.client.memory.util.RangeMap;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;

public class RangeMapTests{

	private static final NavigableMap<Integer,Integer> NAVIGABLE_MAP = Scanner.iterate(10, i -> i + 10)
			.limit(3)
			.toMapSupplied(Function.identity(), TreeMap::new);
	private static final RangeMap<Integer,Integer> MAP = new RangeMap<>(NAVIGABLE_MAP);

	@Test
	public void testEverything(){
		Assert.assertEquals(
				MAP.listValues(Range.everything()),
				List.of(10, 20, 30));
	}

	@Test
	public void testOnlyStartKey(){
		Assert.assertEquals(
				MAP.listValues(new Range<>(0)),
				List.of(10, 20, 30));
	}

	@Test
	public void testOnlyStartInclusive(){
		Assert.assertEquals(
				MAP.listValues(new Range<>(20)),
				List.of(20, 30));
	}

	@Test
	public void testOnlyStartExclusive(){
		Assert.assertEquals(
				MAP.listValues(new Range<Integer>(20, false)),
				List.of(30));
	}

	@Test
	public void testEndInclusive(){
		Assert.assertEquals(
				MAP.listValues(new Range<>(10, false, 30, true)),
				List.of(20, 30));
	}

	@Test
	public void testEndExclusive(){
		Assert.assertEquals(
				MAP.listValues(new Range<>(10, false, 30, false)),
				List.of(20));
	}

	@Test
	public void testInBetweenValues(){
		Assert.assertEquals(
				MAP.listValues(new Range<>(15, 25)),
				List.of(20));
	}

}
