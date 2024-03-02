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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Objects;

/**
 * MilliTime is a time in milliseconds since the epoch. MilliTime is immutable.
 * It is also comparable to other MilliTimes, so it can be used in sorted collections.
 */
public final class MilliTime
implements BaseMilliTime<MilliTime>, Comparable<MilliTime>{

	public static final MilliTime MIN = new MilliTime(0L);
	public static final MilliTime MAX = new MilliTime(Long.MAX_VALUE);

	private final long epochMilli;

	private MilliTime(long epochMilli){
		this.epochMilli = epochMilli;
	}

	public static MilliTime ofEpochMilli(long epochMilli){
		return new MilliTime(epochMilli);
	}

	public static MilliTime ofReversedEpochMilli(long reversedEpochMilli){
		return ofEpochMilli(Long.MAX_VALUE - reversedEpochMilli);
	}

	public static MilliTime of(Instant instant){
		return ofEpochMilli(instant.toEpochMilli());
	}

	public static MilliTime of(Date date){
		return ofEpochMilli(date.getTime());
	}

	public static MilliTime of(ZonedDateTime date){
		return of(date.toInstant());
	}

	/*-----------------------------------------------------------------------*/

	@Override
	public int compareTo(MilliTime other){
		return Long.compare(epochMilli, other.epochMilli);
	}

	@Override
	public String toString(){
		return Long.toString(epochMilli);
	}

	@Override
	public int hashCode(){
		return Long.hashCode(epochMilli);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null || getClass() != obj.getClass()){
			return false;
		}
		MilliTime other = (MilliTime)obj;
		return Objects.equals(epochMilli, other.epochMilli);
	}

	@Override
	public long toEpochMilli(){
		return epochMilli;
	}

	@Override
	public long toReversedEpochMilli(){
		return Long.MAX_VALUE - epochMilli;
	}

	/*-----------------------------------------------------------------------*/

	public static MilliTime now(){
		return ofEpochMilli(System.currentTimeMillis());
	}

	public static MilliTime atStartOfDay(ZoneId zoneId){
		long timeMs = LocalDateTime
				.ofInstant(Instant.now(), zoneId)
				.with(LocalTime.MIN)
				.atZone(zoneId)
				.toInstant()
				.toEpochMilli();
		return ofEpochMilli(timeMs);
	}

	public static MilliTime atEndOfDay(ZoneId zoneId){
		long timeMs = LocalDateTime
				.ofInstant(Instant.now(), zoneId)
				.with(LocalTime.MAX)
				.atZone(zoneId)
				.toInstant()
				.toEpochMilli();
		return ofEpochMilli(timeMs);
	}

	/*--------------------------- mimic java.time ---------------------------*/

	public boolean isBefore(MilliTime milliTime){
		return compareTo(milliTime) < 0;
	}

	public boolean isBeforeOrEqual(MilliTime milliTime){
		return compareTo(milliTime) <= 0;
	}

	public boolean isAfter(MilliTime milliTime){
		return compareTo(milliTime) > 0;
	}

	public boolean isAfterOrEqual(MilliTime milliTime){
		return compareTo(milliTime) >= 0;
	}

	public MilliTime plus(long timeToAdd, TemporalUnit unit){
		return plus(Duration.of(timeToAdd, unit));
	}

	public MilliTime plus(MilliTime milliTime){
		return plus(milliTime.toEpochMilli(), ChronoUnit.MILLIS);
	}

	public MilliTime plus(Duration duration){
		return ofEpochMilli(toEpochMilli() + duration.toMillis());
	}

	public MilliTime minus(long timeToSubtract, TemporalUnit unit){
		return minus(Duration.of(timeToSubtract, unit));
	}

	public MilliTime minus(MilliTime milliTime){
		return minus(milliTime.toEpochMilli(), ChronoUnit.MILLIS);
	}

	public MilliTime minus(Duration duration){
		return ofEpochMilli(toEpochMilli() - duration.toMillis());
	}

	public static MilliTime parse(CharSequence text){
		Instant instant = DateTimeFormatter.ISO_INSTANT.parse(text, Instant::from);
		return MilliTime.of(instant);
	}

	public static MilliTime max(MilliTime time1, MilliTime time2){
		return time1.toEpochMilli() >= time2.toEpochMilli() ? time1 : time2;
	}

	public static MilliTime min(MilliTime time1, MilliTime time2){
		return time1.toEpochMilli() <= time2.toEpochMilli() ? time1 : time2;
	}

}
