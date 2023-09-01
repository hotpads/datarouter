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
package io.datarouter.types;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MilliTimeTests{

	@Test
	public void testCompareToSort(){
		var milliTime1 = MilliTime.ofEpochMilli(4L);
		var milliTime2 = MilliTime.ofEpochMilli(1L);
		var milliTime3 = MilliTime.ofEpochMilli(0L);
		var milliTime4 = MilliTime.ofEpochMilli(3L);
		var milliTime5 = MilliTime.ofEpochMilli(2L);

		List<MilliTime> times = Arrays.asList(
				milliTime1,
				milliTime2,
				milliTime3,
				milliTime4,
				milliTime5);

		Collections.sort(times);

		Assert.assertEquals(times.get(0), milliTime3);
		Assert.assertEquals(times.get(1), milliTime2);
		Assert.assertEquals(times.get(2), milliTime5);
		Assert.assertEquals(times.get(3), milliTime4);
		Assert.assertEquals(times.get(4), milliTime1);
	}

	/*
	 * When passing forward-times to the constructor, the later time should compare before the earlier time.
	 * The comparison should match how the database will sort the rows in the table.
	 */
	@Test
	public void testCompareTo(){
		var milliTime1 = MilliTime.ofEpochMilli(1L);
		var milliTime2 = MilliTime.ofEpochMilli(2L);
		var milliTime3 = MilliTime.ofEpochMilli(2L);

		Assert.assertEquals(milliTime1.compareTo(milliTime2), -1);
		Assert.assertEquals(milliTime2.compareTo(milliTime1), 1);
		Assert.assertEquals(milliTime2.compareTo(milliTime3), 0);
	}

	@Test
	public void testIsBefore(){
		var milliTime1 = MilliTime.ofEpochMilli(1L);
		var milliTime2 = MilliTime.ofEpochMilli(2L);
		Assert.assertTrue(milliTime1.isBefore(milliTime2));
		Assert.assertTrue(milliTime1.toInstant().isBefore(milliTime2.toInstant()));
	}

	@Test
	public void testIsAfter(){
		var milliTime1 = MilliTime.ofEpochMilli(1L);
		var milliTime2 = MilliTime.ofEpochMilli(2L);
		Assert.assertTrue(milliTime2.isAfter(milliTime1));
		Assert.assertTrue(milliTime2.toInstant().isAfter(milliTime1.toInstant()));
	}

	@Test
	public void testPlus(){
		var t1 = MilliTime.ofEpochMilli(10);
		var t2 = t1.plus(Duration.ofMillis(3));
		Assert.assertEquals(t2, MilliTime.ofEpochMilli(13));
	}

	@Test
	public void testPlusWithNanos(){
		var t1 = MilliTime.ofEpochMilli(10);
		var t2 = t1.plus(Duration.ofNanos(10));
		Assert.assertEquals(t2, MilliTime.ofEpochMilli(10));

		var t3 = MilliTime.ofEpochMilli(10);
		var t4 = t3.plus(Duration.ofNanos(10_000_000));
		Assert.assertEquals(t4, MilliTime.ofEpochMilli(20));
	}

	@Test
	public void testMinus(){
		var t1 = MilliTime.ofEpochMilli(10);
		var t2 = t1.minus(Duration.ofMillis(3));
		Assert.assertEquals(t2, MilliTime.ofEpochMilli(7));
	}

	@Test
	public void testMinusWithNanos(){
		var t1 = MilliTime.ofEpochMilli(10);
		var t2 = t1.minus(Duration.ofNanos(10));
		Assert.assertEquals(t2, MilliTime.ofEpochMilli(10));

		var t3 = MilliTime.ofEpochMilli(20);
		var t4 = t3.minus(Duration.ofNanos(10_000_000));
		Assert.assertEquals(t4, MilliTime.ofEpochMilli(10));
	}

	@Test
	public void testComparisonForRefactoring(){
		var time1 = MilliTime.now();
		var time2 = MilliTime.now().minus(Duration.ofDays(1));

		// ">" behavior
		Assert.assertTrue(time1.toEpochMilli() > time2.toEpochMilli());
		Assert.assertTrue(time1.isAfter(time2));

		// "<" behavior
		Assert.assertTrue(time2.toEpochMilli() < time1.toEpochMilli());
		Assert.assertTrue(time2.isBefore(time1));
	}

}
