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

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.datarouter.util.Require;

public class TimeFormula implements TemporalAdjuster{

	private static final String NOW = "now";
	private static final Pattern FORMULA_PATTERN = Pattern.compile("((?<relativeValue>[-+]?\\d+)(?<relativeUnit>\\w+))?"
			+ "(@(?<floorUnit>\\w+))?");

	private final List<TemporalAdjuster> adjusters;

	public TimeFormula(String formula){
		this(formula, DEFAULT_UNITS);
	}

	public TimeFormula(String formula, List<TimeModifierUnit> units) throws IllegalArgumentException{
		if(formula.equals(NOW)){
			adjusters = List.of();
			return;
		}

		adjusters = new ArrayList<>();

		boolean anyMatches = false;
		Matcher matcher = FORMULA_PATTERN.matcher(formula);
		while(matcher.find()){
			if(matcher.group().isEmpty()){
				continue;
			}

			anyMatches = true;

			List<TemporalAdjuster> groupAdjusters = new ArrayList<>();
			Optional.ofNullable(matcher.group("floorUnit"))
					.map(unit -> determineUnit(unit, units))
					.map(TimeModifierUnit::floor)
					.ifPresent(groupAdjusters::add);

			Optional.ofNullable(matcher.group("relativeUnit"))
					.map(unit -> determineUnit(unit, units))
					.map(TimeModifierUnit::offset)
					.map(scaled -> {
						int scale = Optional.ofNullable(matcher.group("relativeValue"))
								.map(Integer::parseInt)
								.orElseThrow(() -> new IllegalArgumentException("No offset scale supplied, at="
										+ matcher.group()));
						return scaled.getScaled(scale);
					})
					.ifPresent(groupAdjusters::add);

			if(groupAdjusters.isEmpty()){
				throw new IllegalArgumentException("Invalid time formula=" + formula + " at=" + matcher.group());
			}

			adjusters.addAll(groupAdjusters);
		}
		if(!anyMatches){
			throw new IllegalArgumentException("Invalid time formula=" + formula);
		}
	}

	public ZonedDateTime apply(ZonedDateTime relativeNow){
		return relativeNow.with(this);
	}

	@Override
	public Temporal adjustInto(Temporal temporal){
		Temporal result = temporal;
		for(TemporalAdjuster adjuster : adjusters){
			result = result.with(adjuster);
		}
		return result;
	}

	private static TimeModifierUnit determineUnit(String unit, List<TimeModifierUnit> units){
		for(TimeModifierUnit timeUnit : units){
			if(timeUnit.units.contains(unit)){
				return timeUnit;
			}
		}
		throw new IllegalArgumentException("No units matching input=" + unit);
	}

	public static final TimeModifierUnit
			MINUTE = new TimeModifierUnit(
					Set.of("m"),
					temp -> temp.with(ChronoField.SECOND_OF_MINUTE, 0).with(ChronoField.MILLI_OF_SECOND, 0),
					scale -> temp -> temp.plus(scale, ChronoUnit.MINUTES)),
			HOUR = new TimeModifierUnit(
					Set.of("h"),
					temp -> temp.with(MINUTE.floor).with(ChronoField.MINUTE_OF_HOUR, 0),
					scale -> temp -> temp.plus(scale, ChronoUnit.HOURS)),
			DAY = new TimeModifierUnit(
					Set.of("d"),
					temp -> temp.with(HOUR.floor).with(ChronoField.HOUR_OF_DAY, 0),
					scale -> temp -> temp.plus(scale, ChronoUnit.DAYS)),
			WEEK = new TimeModifierUnit(
					Set.of("w"),
					temp -> temp.with(DAY.floor).with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)),
					scale -> temp -> temp.plus(scale, ChronoUnit.WEEKS)),
			MONTH = new TimeModifierUnit(
					Set.of("mo"),
					temp -> temp.with(DAY.floor).with(TemporalAdjusters.firstDayOfMonth()),
					scale -> temp -> temp.plus(scale, ChronoUnit.MONTHS)),
			YEAR = new TimeModifierUnit(
					Set.of("y"),
					temp -> temp.with(DAY.floor).with(TemporalAdjusters.firstDayOfYear()),
					scale -> temp -> temp.plus(scale, ChronoUnit.YEARS));

	public static final List<TimeModifierUnit> DEFAULT_UNITS = List.of(MINUTE, HOUR, DAY, WEEK, MONTH, YEAR);

	public record TimeModifierUnit(
			Set<String> units,
			TemporalAdjuster floor,
			ScaledTemporalAdjuster offset){

		public TimeModifierUnit{
			Require.notEmpty(units, "Units cannot be empty");
		}

		public TimeModifierUnit(Set<String> units, ScaledTemporalAdjuster offset){
			this(units,
					temp -> {
						throw new RuntimeException("Unit " + units.iterator().next() + " does not support floor");
					},
					offset);
		}

	}

	public interface ScaledTemporalAdjuster{
		TemporalAdjuster getScaled(int scale);
	}

}
