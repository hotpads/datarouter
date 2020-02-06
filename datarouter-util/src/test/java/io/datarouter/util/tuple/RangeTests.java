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
package io.datarouter.util.tuple;

import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.ComparableTool;

public class RangeTests{

	@Test
	public void testContains(){
		Range<Integer> rangeA = new Range<>(3, true, 5, true);
		Assert.assertFalse(rangeA.contains(2));
		Assert.assertTrue(rangeA.contains(3));
		Assert.assertTrue(rangeA.contains(5));
		Assert.assertFalse(rangeA.contains(6));

		Range<Integer> rangeB = new Range<>(3, false, 5, false);
		Assert.assertFalse(rangeB.contains(3));
		Assert.assertTrue(rangeB.contains(4));
		Assert.assertFalse(rangeB.contains(5));

		Range<Integer> rangeC = new Range<>(7, true, 7, true);
		Assert.assertTrue(rangeC.contains(7));

		Range<Integer> rangeD = new Range<>(8, false, 8, false);
		Assert.assertFalse(rangeD.contains(8));

		Range<Integer> rangeE = new Range<>(9, true, 9, false);// exclusive should win (?)
		Assert.assertFalse(rangeE.contains(9));
	}

	@Test
	public void testCompareStarts(){
		Range<Integer> rangeA = new Range<>(null, true, null, true);
		Assert.assertEquals(Range.compareStarts(rangeA, rangeA), 0);

		Range<Integer> rangeB = new Range<>(null, false, null, true);
		Assert.assertEquals(compareAndAssertReflexive(rangeA, rangeB), -1);

		Range<Integer> rangeC = new Range<>(null, true, 999, true);
		Assert.assertEquals(compareAndAssertReflexive(rangeA, rangeC), 0);

		Range<Integer> rangeD = new Range<>(3, true, 999, true);
		Assert.assertEquals(compareAndAssertReflexive(rangeA, rangeD), -1);

		Range<Integer> rangeE = new Range<>(3, false, 999, true);
		Assert.assertEquals(compareAndAssertReflexive(rangeA, rangeD), -1);

		Range<Integer> rangeF = new Range<>(4, true, 999, true);
		Assert.assertEquals(compareAndAssertReflexive(rangeD, rangeF), -1);
		Assert.assertEquals(compareAndAssertReflexive(rangeE, rangeF), -1);

		Range<Integer> rangeG = new Range<>(4, false, 999, true);
		Assert.assertEquals(compareAndAssertReflexive(rangeD, rangeG), -1);
		Assert.assertEquals(compareAndAssertReflexive(rangeE, rangeG), -1);
		Assert.assertEquals(compareAndAssertReflexive(rangeF, rangeG), -1);
	}

	@Test
	public void testValidAssert(){
		new Range<>(null, null).assertValid();
		new Range<>(0, null).assertValid();
		new Range<>(null, 0).assertValid();
		new Range<>(0, 1).assertValid();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testInvalidAssert(){
		new Range<>(1, 0).assertValid();
	}

	@Test
	public void testMap(){
		Range<Integer> input = new Range<>(1, true, 3, true);
		Function<Integer,String> mapper = i -> i + "%";
		Range<String> expected = new Range<>("1%", true, "3%", true);
		Range<String> actual = input.map(mapper);
		Assert.assertEquals(actual, expected);
	}

	public static <T extends Comparable<? super T>> int compareAndAssertReflexive(T object1, T object2){
		int forwardDiff = ComparableTool.nullFirstCompareTo(object1, object2);
		int backwardDiff = ComparableTool.nullFirstCompareTo(object2, object1);
		Assert.assertEquals(-backwardDiff, forwardDiff);
		return forwardDiff;
	}

}
