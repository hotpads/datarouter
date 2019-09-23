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
package io.datarouter.util.duration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DurationWithCarriedUnitsTests{

	@Test
	public void testTypicalUse(){
		DurationWithCarriedUnits wpd;

		wpd = new DurationWithCarriedUnits(convert(0, 0, 5,2,31,7,521));
		Assert.assertEquals(wpd.toStringByMaxUnits(2), "5 days, 2 hours");
	}

	@Test
	public void testFullStack(){
		DurationWithCarriedUnits wpd;

		wpd = new DurationWithCarriedUnits(convert(0, 0, 5,2,31,7,521));
		Assert.assertEquals(wpd.toString(), "5 days, 2 hours, 31 minutes, 7 seconds, 521 milliseconds");

		wpd = new DurationWithCarriedUnits(convert(0, 0, 5,2,31,7,521));
		Assert.assertEquals(wpd.toStringByMaxPrecision(DurationUnit.MILLISECONDS),
				"5 days, 2 hours, 31 minutes, 7 seconds, 521 milliseconds");

		wpd = new DurationWithCarriedUnits(convert(0, 0, 5,2,31,7,521));
		Assert.assertEquals(wpd.toStringByMaxUnits(Integer.MAX_VALUE),
				"5 days, 2 hours, 31 minutes, 7 seconds, 521 milliseconds");
	}

	@Test
	public void testTruncation(){
		DurationWithCarriedUnits wpd;

		wpd = new DurationWithCarriedUnits(convert(0, 0, 0,0,0,7,521));
		Assert.assertEquals(wpd.toStringByMaxUnitsMaxPrecision(DurationUnit.SECONDS,2), "7 seconds");

		wpd = new DurationWithCarriedUnits(convert(0, 0, 0,2,31,7,521));
		Assert.assertEquals(wpd.toStringByMaxPrecision(DurationUnit.DAYS), "less than one day");

		wpd = new DurationWithCarriedUnits(convert(0, 0, 5,0,31,7,521));
		Assert.assertEquals(wpd.toStringByMaxUnits(2), "5 days");

	}

	@Test
	public void testLessThan(){
		DurationWithCarriedUnits wpd;

		wpd = new DurationWithCarriedUnits(convert(0, 0, 0,2,31,7,521));
		Assert.assertEquals(wpd.toStringByMaxPrecision(DurationUnit.DAYS), "less than one day");

		wpd = new DurationWithCarriedUnits(convert(0, 0, 0,0,0,0,521));
		Assert.assertEquals(wpd.toStringByMaxUnitsMaxPrecision(DurationUnit.SECONDS,2), "less than one second");

	}

	@Test
	public void testSingularUnits(){
		DurationWithCarriedUnits wpd;

		wpd = new DurationWithCarriedUnits(convert(0, 0, 5,1,1,1,521));
		Assert.assertEquals(wpd.toStringByMaxUnits(2), "5 days, 1 hour");

	}

	@Test
	public void testMonths(){
		long millis = convert(0, 3,5,0,0,0,0);
		DurationWithCarriedUnits wpd = new DurationWithCarriedUnits(millis);
		Assert.assertEquals(wpd.toStringByMaxUnits(1), "3 months");
	}

	@Test
	public void testYears(){
		long millis = convert(2,3,0,0,0,0,0);
		DurationWithCarriedUnits wpd = new DurationWithCarriedUnits(millis);
		Assert.assertEquals(wpd.toStringByMaxUnits(1), "2 years");
		Assert.assertEquals(wpd.toStringByMaxUnits(2), "2 years, 3 months");

	}

	private static long convert(int years, int months, int day, int hour, int minute, int sec, int ms){
		long millis = 0;
		millis += ms;
		millis += sec * 1000L;
		millis += minute * 1000L * 60;
		millis += hour * 1000L * 60 * 60;
		millis += day * 1000L * 60 * 60 * 24;
		millis += (long) (months * (365.0 / 12) * 1000L * 60 * 60 * 24);
		millis += years * 365 * 1000L * 60 * 60 * 24;
		return millis;
	}

}