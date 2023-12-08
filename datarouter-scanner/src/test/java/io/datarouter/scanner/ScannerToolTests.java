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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

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
	public void testCollection(){
		List<Integer> inputs = List.of(2, 1, 3, 1);
		Assert.assertEquals(Scanner.of(inputs).collect(ArrayList::new), List.of(2, 1, 3, 1));
		Assert.assertEquals(Scanner.of(inputs).collect(LinkedList::new), List.of(2, 1, 3, 1));
		Assert.assertEquals(Scanner.of(inputs).collect(TreeSet::new), List.of(1, 2, 3));
		Assert.assertEquals(Scanner.of(inputs).collect(LinkedHashSet::new), List.of(2, 1, 3));
	}

	@Test
	public void testFindFirst(){
		Assert.assertEquals(Scanner.of(1, 3, 5).findFirst().get().intValue(), 1);
		Assert.assertFalse(Scanner.empty().findFirst().isPresent());
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testFindFirstNull(){
		Scanner.of(null, 1).findFirst();
	}

	@Test
	public void testFindLast(){
		Assert.assertEquals(Scanner.of(1, 3, 5).findLast().get().intValue(), 5);
		Assert.assertFalse(Scanner.empty().findLast().isPresent());
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testFindLastNull(){
		Scanner.of(1, null).findLast();
	}

	@Test
	public void testFlush(){
		List<Integer> snapshot = new ArrayList<>();
		List<Integer> collected = Scanner.of(1, 2, 3)
				.flush(snapshot::addAll)
				.list();
		Assert.assertEquals(snapshot.size(), 3);
		Assert.assertEquals(snapshot, collected);
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
	public void testFindMax(){
		Assert.assertFalse(Scanner.of(1).skip(1).findMax(Comparator.naturalOrder()).isPresent());
		Assert.assertEquals(Scanner.of(2, 1, 3).findMax(Comparator.naturalOrder()).get().intValue(), 3);
	}

	@Test
	public void testFindMin(){
		Assert.assertFalse(Scanner.of(1).skip(1).findMin(Comparator.naturalOrder()).isPresent());
		Assert.assertEquals(Scanner.of(2, 1, 3).findMin(Comparator.naturalOrder()).get().intValue(), 1);
	}

	@Test
	public void testMaxN(){
		List<Integer> inputs = List.of(4, 0, 1, 0, 3, 3, 3, 2);
		Comparator<Integer> comparator = Comparator.naturalOrder();
		List<Integer> max0 = ScannerTool.maxNDesc(Scanner.of(inputs), comparator, 0).list();
		Assert.assertEquals(max0, List.of());
		List<Integer> max1 = ScannerTool.maxNDesc(Scanner.of(inputs), comparator, 1).list();
		Assert.assertEquals(max1, List.of(4));
		List<Integer> max2 = ScannerTool.maxNDesc(Scanner.of(inputs), comparator, 2).list();
		Assert.assertEquals(max2, List.of(4, 3));
		List<Integer> maxAll = ScannerTool.maxNDesc(Scanner.of(inputs), comparator, inputs.size() * 2).list();
		Assert.assertEquals(maxAll, Scanner.of(inputs).sort(comparator.reversed()).list());
	}

	@Test
	public void testMinN(){
		List<Integer> inputs = List.of(4, 0, 1, 0, 3, 3, 3, 2);
		Comparator<Integer> comparator = Comparator.naturalOrder();
		List<Integer> min0 = ScannerTool.minNAsc(Scanner.of(inputs), comparator, 0).list();
		Assert.assertEquals(min0, List.of());
		List<Integer> min1 = ScannerTool.minNAsc(Scanner.of(inputs), comparator, 1).list();
		Assert.assertEquals(min1, List.of(0));
		List<Integer> min2 = ScannerTool.minNAsc(Scanner.of(inputs), comparator, 2).list();
		Assert.assertEquals(min2, List.of(0, 0));
		List<Integer> minAll = ScannerTool.minNAsc(Scanner.of(inputs), comparator, inputs.size() * 2).list();
		Assert.assertEquals(minAll, Scanner.of(inputs).sort(comparator).list());
	}

	@Test
	public void testNoneMatch(){
		Assert.assertTrue(Scanner.of(1, 3, 5).noneMatch(i -> i % 2 == 0));
		Assert.assertFalse(Scanner.of(1, 2, 5).noneMatch(i -> i % 2 == 0));
	}

	@Test
	public void testReduce(){
		Scanner<Integer> input = Scanner.of(2, 1, 4, 5, 3);
		BinaryOperator<Integer> reducer = Math::max;
		int expected = 5;
		int actual = input.reduce(reducer).get();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testReduceEmpty(){
		List<Integer> input = List.of();
		BinaryOperator<Integer> reducer = Math::max;
		Optional<Integer> actual = Scanner.of(input).reduce(reducer);
		Assert.assertFalse(actual.isPresent());
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testReduceNullItem(){
		Scanner<Integer> input = Scanner.of(null, 1);
		BinaryOperator<Integer> reducer = Math::max;
		input.reduce(reducer);
	}

	@Test
	public void testReduceWithSeed(){
		Scanner<Integer> input = Scanner.of(2, 1, 4, 5, 3);
		BinaryOperator<Integer> reducer = Math::max;
		int expected = 6;
		int actual = input.reduce(6, reducer);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testReduceWithSeedEmpty(){
		List<Integer> input = List.of();
		BinaryOperator<Integer> reducer = Math::max;
		int actual = Scanner.of(input).reduce(6, reducer);
		Assert.assertEquals(actual, 6);
	}

	@Test
	public void testSkip(){
		Scanner<Integer> input = Scanner.of(1, 2, 3, 4);
		List<Integer> actual = input.skip(2).list();
		List<Integer> expected = List.of(3, 4);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testTake(){
		Scanner<Integer> input = Scanner.of(1, 2, 3, 4);
		Assert.assertEquals(input.take(3), List.of(1, 2, 3));
		Assert.assertEquals(input.take(3), List.of(4));
	}

	@Test
	public void testToArray(){
		Assert.assertEquals(Scanner.of(1, 2, 3).toArray(), new Object[]{1, 2, 3});
		Assert.assertEquals(Scanner.empty().toArray(), new Object[]{});
	}

}
