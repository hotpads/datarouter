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
package io.datarouter.util.incrementor;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LocalDateTimeIncrementorTests{

	@Test
	public void testMinutes(){
		LocalDateTime start = LocalDateTime.of(2019, Month.JULY, 20, 15, 12, 3);
		List<LocalDateTime> actual = LocalDateTimeIncrementor.fromInclusive(start, ChronoUnit.MINUTES)
				.step(2)
				.limit(2)
				.list();
		List<LocalDateTime> expected = Arrays.asList(
				start,
				LocalDateTime.of(2019, Month.JULY, 20, 15, 14, 3));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testWeeks(){
		LocalDateTime start = LocalDateTime.of(2019, Month.JULY, 20, 15, 12, 3);
		List<LocalDateTime> actual = LocalDateTimeIncrementor.fromInclusive(start, ChronoUnit.WEEKS)
				.limit(3)
				.list();
		List<LocalDateTime> expected = Arrays.asList(
				start,
				LocalDateTime.of(2019, Month.JULY, 27, 15, 12, 3),
				LocalDateTime.of(2019, Month.AUGUST, 3, 15, 12, 3));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testLeapYearMinutes(){
		LocalDateTime start = LocalDateTime.of(2016, Month.FEBRUARY, 28, 23, 55, 3);
		List<LocalDateTime> actual = LocalDateTimeIncrementor.fromInclusive(start, ChronoUnit.MINUTES)
				.step(10)
				.limit(2)
				.list();
		List<LocalDateTime> expected = Arrays.asList(
				start,
				LocalDateTime.of(2016, Month.FEBRUARY, 29, 0, 5, 3));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testLeapYearHours(){
		LocalDateTime start = LocalDateTime.of(2016, Month.FEBRUARY, 28, 20, 9, 3);
		List<LocalDateTime> actual = LocalDateTimeIncrementor.fromInclusive(start, ChronoUnit.HOURS)
				.step(18)
				.limit(3)
				.list();
		List<LocalDateTime> expected = Arrays.asList(
				start,
				LocalDateTime.of(2016, Month.FEBRUARY, 29, 14, 9, 3),
				LocalDateTime.of(2016, Month.MARCH, 1, 8, 9, 3));
		Assert.assertEquals(actual, expected);
	}

}
