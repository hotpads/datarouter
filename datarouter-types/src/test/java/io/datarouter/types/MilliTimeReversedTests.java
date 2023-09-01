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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MilliTimeReversedTests{

	@Test
	public void testCompareToSort(){
		var milliTime0 = MilliTimeReversed.ofReversedEpochMilli(4L);
		var milliTime1 = MilliTimeReversed.ofReversedEpochMilli(1L);
		var milliTime2 = MilliTimeReversed.ofReversedEpochMilli(0L);
		var milliTime3 = MilliTimeReversed.ofReversedEpochMilli(3L);
		var milliTime4 = MilliTimeReversed.ofReversedEpochMilli(2L);

		List<MilliTimeReversed> times = Arrays.asList(
				milliTime0,
				milliTime1,
				milliTime2,
				milliTime3,
				milliTime4);

		Collections.sort(times);

		Assert.assertEquals(times.get(0), milliTime2);
		Assert.assertEquals(times.get(1), milliTime1);
		Assert.assertEquals(times.get(2), milliTime4);
		Assert.assertEquals(times.get(3), milliTime3);
		Assert.assertEquals(times.get(4), milliTime0);
	}

	/*
	 * When passing forward-times to the constructor, the later time should compare before the earlier time.
	 * The comparison should match how the database will sort the rows in the table.
	 */
	@Test
	public void testCompareTo(){
		var milliTime1 = MilliTimeReversed.ofReversedEpochMilli(1L);
		var milliTime2 = MilliTimeReversed.ofReversedEpochMilli(2L);
		var milliTime3 = MilliTimeReversed.ofReversedEpochMilli(2L);

		Assert.assertEquals(milliTime1.compareTo(milliTime2), -1);
		Assert.assertEquals(milliTime2.compareTo(milliTime1), 1);
		Assert.assertEquals(milliTime2.compareTo(milliTime3), 0);
	}

}
