/**
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
package io.datarouter.util.iterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IterableToolTests{

	@Test
	public void testMap(){
		List<String> names = Arrays.asList("Al", "Bob");
		List<String> greetings = IterableTool.map(names, name -> "Hello " + name);
		Assert.assertEquals(greetings.getClass(), ArrayList.class);//prefer ArrayList to LinkedList
		Assert.assertEquals(greetings.size(), 2);
		Assert.assertEquals(greetings.get(0), "Hello Al");
		Assert.assertEquals(greetings.get(1), "Hello Bob");
	}

	@Test
	public void testMapToSet(){
		List<String> names = Arrays.asList("Al", "Bob", "Bob");
		Set<String> greetings = IterableTool.mapToSet(names, name -> "Hello " + name);
		Assert.assertEquals(greetings.getClass(), HashSet.class);
		Assert.assertEquals(greetings.size(), 2);
		Assert.assertTrue(greetings.contains("Hello Al"));
		Assert.assertTrue(greetings.contains("Hello Bob"));
	}

	@Test
	public void testFilter(){
		List<String> names = Arrays.asList("Al", "Bob");
		List<String> filtered = IterableTool.filter(names, name -> name.equals(names.get(0)));
		Assert.assertEquals(filtered.size(), 1);
		Assert.assertEquals(filtered.get(0), names.get(0));
	}

	@Test
	public void testInclude(){
		List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);
		List<Integer> filtered = IterableTool.include(nums, num -> num % 2 == 0);
		Assert.assertEquals(filtered.size(), 2);
		Assert.assertEquals(filtered.get(0), Integer.valueOf(2));
	}

	@Test
	public void testExclude(){
		List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);
		List<Integer> filtered = IterableTool.exclude(nums, num -> num % 2 == 0);
		Assert.assertEquals(filtered.size(), 3);
		Assert.assertEquals(filtered.get(0), Integer.valueOf(1));
	}

	@Test
	public void testSkip(){
		List<Integer> original = IntStream.range(0, 10).boxed().collect(Collectors.toList());
		Assert.assertEquals(IterableTool.skip(original, null), original);
		Assert.assertEquals(IterableTool.skip(original, 0L), original);
		Assert.assertEquals(IterableTool.skip(original, 3L), original.subList(3, 10));
		Assert.assertEquals(IterableTool.skip(original, 15L), new ArrayList<>());
	}

	@Test
	public void testForEach(){
		List<Integer> inputs = Arrays.asList(3, 7);
		AtomicLong total = new AtomicLong();
		IterableTool.forEach(inputs, input -> total.addAndGet(input));
		Assert.assertEquals(total.get(), 10);
	}

}
