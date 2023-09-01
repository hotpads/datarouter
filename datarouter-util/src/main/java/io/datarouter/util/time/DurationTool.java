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
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import io.datarouter.util.duration.DurationUnit;
import io.datarouter.util.duration.DurationWithCarriedUnits;

public class DurationTool{

	public static final double AVG_DAYS_PER_MONTH = 30.436875;
	public static final long AVG_SECONDS_PER_MONTH = (long)(AVG_DAYS_PER_MONTH * 24 * 60 * 60);
	public static final Duration AVG_MONTH = Duration.ofSeconds(AVG_SECONDS_PER_MONTH);
	public static final double AVG_DAYS_PER_YEAR = 365.2425;
	public static final long AVG_SECONDS_PER_YEAR = (long)(AVG_DAYS_PER_YEAR * 24 * 60 * 60);
	public static final Duration AVG_YEAR = Duration.ofSeconds(AVG_SECONDS_PER_YEAR);
	public static final double AVG_MONTHS_PER_YEAR = AVG_DAYS_PER_YEAR / AVG_DAYS_PER_MONTH;

	public static Duration sinceDate(Date date){
		Objects.requireNonNull(date);
		return Duration.ofMillis(System.currentTimeMillis() - date.getTime());
	}

	@Deprecated // inline
	public static Duration sinceInstant(Instant from){
		Objects.requireNonNull(from);
		return Duration.between(from, Instant.now());
	}

	public static String toString(Duration duration){
		var wud = new DurationWithCarriedUnits(duration.toMillis());
		return wud.toStringByMaxUnitsMaxPrecision(DurationUnit.MILLISECONDS, 2);
	}

}
