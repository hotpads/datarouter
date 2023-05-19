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
package io.datarouter.util.time;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.time.TimeFormula.TimeModifierUnit;

public class TimeFormulaTests{

	private static final ZonedDateTime NOW = ZonedDateTime.of(2023, 3, 15, 3, 46, 15, 7, ZoneIds.UTC);

	@Test
	public void testTimeFormula(){
		assertEquals("@d", ZonedDateTime.of(2023, 3, 15, 0, 0, 0, 0, ZoneIds.UTC));
		assertEquals("12m", ZonedDateTime.of(2023, 3, 15, 3, 58, 15, 7, ZoneIds.UTC));
		assertEquals("12m@h", ZonedDateTime.of(2023, 3, 15, 3, 12, 0, 0, ZoneIds.UTC));
		assertEquals("5y@y", ZonedDateTime.of(2028, 1, 1, 0, 0, 0, 0, ZoneIds.UTC));
		assertEquals("5mo@y+8h-12m", ZonedDateTime.of(2023, 6, 1, 7, 48, 0, 0, ZoneIds.UTC));
	}

	@Test
	public void testCustomTimeFormulaUnit(){
		var customUnit = new TimeModifierUnit(
				Set.of("z"),
				temp -> {
					int mins = temp.get(ChronoField.MINUTE_OF_HOUR);
					return temp.with(ChronoField.MINUTE_OF_HOUR, mins - mins % 15);
				},
				scale -> temp -> temp.plus(Duration.ofMinutes(15).multipliedBy(scale)));

		assertEquals(
				new TimeFormula("@z", List.of(customUnit, TimeFormula.DAY)),
				ZonedDateTime.of(2023, 3, 15, 3, 45, 15, 7, ZoneIds.UTC));

		assertEquals(
				new TimeFormula("3z@d", List.of(customUnit, TimeFormula.DAY)),
				ZonedDateTime.of(2023, 3, 15, 0, 45, 0, 0, ZoneIds.UTC));
	}

	private void assertEquals(String formula, ZonedDateTime expected){
		assertEquals(new TimeFormula(formula), expected);
	}

	private void assertEquals(TimeFormula timeFormula, ZonedDateTime expected){
		ZonedDateTime actual = NOW.with(timeFormula);
		Assert.assertEquals(actual, expected);
	}

}
