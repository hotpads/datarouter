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
package io.datarouter.scanner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ScannerToolTests{

	@Test
	public void testAllMatch(){
		Assert.assertTrue(Scanner.of(1, 3, 5).allMatch(i -> i % 2 == 1));
		Assert.assertFalse(Scanner.of(1, 2, 5).allMatch(i -> i % 2 == 1));
	}

	@Test
	public void testAnyMatch(){
		Assert.assertTrue(Scanner.of(1, 3, 5).anyMatch(i -> i % 2 == 1));
		Assert.assertFalse(Scanner.of(0, 2, 4).anyMatch(i -> i % 2 == 1));
	}

	@Test
	public void testFindAny(){
		List<Integer> items = Arrays.asList(1, 2, 3);
		Integer any = Scanner.of(items).findAny().get();
		Assert.assertTrue(items.contains(any));
		Assert.assertFalse(Scanner.empty().findAny().isPresent());
	}

	@Test
	public void testFindFirst(){
		Assert.assertEquals(Scanner.of(1, 3, 5).findFirst().get().intValue(), 1);
		Assert.assertFalse(Scanner.empty().findFirst().isPresent());
	}

	@Test
	public void testFindLast(){
		Assert.assertEquals(Scanner.of(1, 3, 5).findLast().get().intValue(), 5);
		Assert.assertFalse(Scanner.empty().findLast().isPresent());
	}

	@Test
	public void testHasAny(){
		Assert.assertTrue(Scanner.of(1, 2).hasAny());
		Assert.assertFalse(Scanner.empty().hasAny());
	}

	@Test
	public void testIsEmpty(){
		Assert.assertTrue(Scanner.empty().isEmpty());
		Assert.assertFalse(Scanner.of(1, 2).isEmpty());
	}

	@Test
	public void testMax(){
		Assert.assertFalse(Scanner.of(1).skip(1).max(Comparator.naturalOrder()).isPresent());
		Assert.assertEquals(Scanner.of(2, 1, 3).max(Comparator.naturalOrder()).get().intValue(), 3);
	}

	@Test
	public void testMin(){
		Assert.assertFalse(Scanner.of(1).skip(1).min(Comparator.naturalOrder()).isPresent());
		Assert.assertEquals(Scanner.of(2, 1, 3).min(Comparator.naturalOrder()).get().intValue(), 1);
	}

	@Test
	public void testNoneMatch(){
		Assert.assertTrue(Scanner.of(1, 3, 5).noneMatch(i -> i % 2 == 0));
		Assert.assertFalse(Scanner.of(1, 2, 5).noneMatch(i -> i % 2 == 0));
	}

	@Test
	public void testSkip(){
		Scanner<Integer> input = Scanner.of(1, 2, 3, 4);
		List<Integer> actual = input.skip(2).list();
		List<Integer> expected = Arrays.asList(3, 4);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testTake(){
		Scanner<Integer> input = Scanner.of(1, 2, 3, 4);
		Assert.assertEquals(input.take(3), Arrays.asList(1, 2, 3));
		Assert.assertEquals(input.take(3), Arrays.asList(4));
	}

	@Test
	public void testToArray(){
		Assert.assertEquals(Scanner.of(1, 2, 3).toArray(), new Object[]{1, 2, 3});
		Assert.assertEquals(Scanner.empty().toArray(), new Object[]{});
	}

}