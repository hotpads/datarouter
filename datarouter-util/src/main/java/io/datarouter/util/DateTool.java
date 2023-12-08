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
package io.datarouter.util;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.types.MilliTime;
import io.datarouter.util.duration.DurationUnit;
import io.datarouter.util.duration.DurationWithCarriedUnits;

/*
 * @Deprecated use java.time.* or MilliTme instead
 */
@Deprecated
public class DateTool{

	public static final long MILLISECONDS_IN_DAY = Duration.ofDays(1).toMillis();
	public static final long MILLISECONDS_IN_HOUR = Duration.ofHours(1).toMillis();
	public static final long MILLISECONDS_IN_MINUTE = Duration.ofMinutes(1).toMillis();
	public static final long MILLISECONDS_IN_SECOND = Duration.ofSeconds(1).toMillis();

	public static final Set<DayOfWeek> WEEK_DAYS = Set.of(
			DayOfWeek.MONDAY,
			DayOfWeek.TUESDAY,
			DayOfWeek.WEDNESDAY,
			DayOfWeek.THURSDAY,
			DayOfWeek.FRIDAY);
	public static final Set<DayOfWeek> WEEKEND_DAYS = Set.of(
			DayOfWeek.SATURDAY,
			DayOfWeek.SUNDAY);

	//TODO pass zoneId instead of using systemDefault
	public static String getDateTime(Date date){
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime()
				.format(dateTimeFormatter);
	}

	/*---------------- time elapsed ----------------*/

	public static boolean hasPassed(Date date){
		return new Date().after(date);
	}

	public static long getMillisecondDifference(Date d1, Date d2){
		return d2.getTime() - d1.getTime();
	}

	/**
	 * note: 24 hr periods. partial days like DST switch and leap seconds are not 24 hours. use getDatesBetween
	 */
	public static double getSecondsBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_SECOND);
	}

	public static double getMinutesBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_MINUTE);
	}

	public static double getPeriodsBetween(Date d1, Date d2, long periodLengthMs){
		long msDif = Math.abs(getMillisecondDifference(d1, d2));
		return msDif / (double)periodLengthMs;
	}

	public static String getInternetDate(TemporalAccessor temporalValue){
		return DateTimeFormatter.ISO_INSTANT.format(temporalValue);
	}

	/* as specified in RFC 3339 / ISO 8601 with second precision only*/
	public static String getInternetDate(Date date){
		return getInternetDate(date, 0);
	}

	private static final ConcurrentHashMap<Integer,DateTimeFormatter> DATE_TIME_MS_FORMATS = new ConcurrentHashMap<>();

	public static String getInternetDate(Date date, int msCount){
		DateTimeFormatter dateTimeFormat = DATE_TIME_MS_FORMATS.computeIfAbsent(msCount,
				DateTool::makeDateTimeFormatter);
		return dateTimeFormat.format(date.toInstant());
	}

	private static DateTimeFormatter makeDateTimeFormatter(int msCount){
		StringBuilder sb = new StringBuilder();
		sb.append("yyyy-MM-dd'T'HH:mm:ss");
		if(msCount > 0){
			sb.append('.');
			for(int i = 0; i < msCount; i++){
				sb.append('S');
			}
		}
		sb.append("'Z'");
		return DateTimeFormatter.ofPattern(sb.toString())
				.withZone(ZoneId.from(ZoneOffset.UTC));
	}

	public static Date parseIso(String str){
		Instant instant = DateTimeFormatter.ISO_INSTANT.parse(str, Instant::from);
		return Date.from(instant);
	}

	public static String getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(Long ms){
		return format("yyyy-MM-dd_HH:mm:ss.SSS", ms);
	}

	public static String format(String pattern, Long ms){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault()).format(formatter);
	}

	public static final int DEFAULT_MAX_UNITS = 2;

	/**
	 * get the date in the form of "XX days, XX hours ago"
	 * only returns minutes if less than 3 hours ago, and only seconds if
	 * 	less than 1 minute ago
	 *
	 * @return "XXXXdays, XX hours ago" or "XX minutes ago" or
	 * 	"XX seconds ago" or "less than one second ago"
	 */
	public static String getAgoString(Instant instant){
		return getAgoString(instant, DEFAULT_MAX_UNITS);
	}

	public static String getAgoString(Long dateMs){
		return getAgoString(Instant.ofEpochMilli(dateMs));
	}

	public static String getAgoString(Instant instant, int maxUnits){
		if(instant == null){
			return null;
		}
		long timeMillis = instant.until(Instant.now(), ChronoUnit.MILLIS);
		String suffix = " ago";
		if(timeMillis < 0){
			suffix = " from now";
		}
		return getMillisAsString(Math.abs(timeMillis), maxUnits, DurationUnit.SECONDS) + suffix;
	}

	/**
	 * Translates the given milliseconds into a human readable time
	 * string to the specified precision.
	 * @param maxUnits - the desired maximum number of units in the returned string.
	 * @return a labeled,
	 */
	public static String getMillisAsString(long timeMillis, int maxUnits, DurationUnit maxPrecision){
		var wud = new DurationWithCarriedUnits(timeMillis);
		return wud.toStringByMaxUnitsMaxPrecision(maxPrecision, maxUnits);
	}

	//TODO pass zoneId instead of using systemDefault
	public static Date getDaysAgo(int daysAgo, Date fromDate){
		Instant pastIntsant = fromDate.toInstant()
				.atZone(ZoneId.systemDefault())
				.minusDays(daysAgo)
				.toInstant();
		return Date.from(pastIntsant);
	}

	public static Date getDaysAgo(int daysAgo){
		return getDaysAgo(daysAgo, new Date());
	}

	public static double getDaysBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_DAY);
	}

	/*---------------- day of week ----------------*/

	// Note: These should be changed to use ZoneId and Instant as parameter, and ultimately removed. Calculating the
	// day of Date is technically incorrect. You require a timezone to be able to precisely tell.
	public static boolean isWeekend(Date date){
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return WEEKEND_DAYS.contains(zonedDateTime.getDayOfWeek());
	}

	/*---------------- reverse ------------------*/

	@Deprecated // use MilliTime and MilliTimeReversed
	public static Long toReverseInstantLong(Instant instant){
		if(instant == null){
			return null;
		}
		return MilliTime.of(instant).toReversedEpochMilli();
	}

	@Deprecated // use MilliTime and MilliTimeReversed
	public static Long toReverseLong(Long forwardTimeMs){
		if(forwardTimeMs == null){
			return null;
		}
		return MilliTime.ofEpochMilli(forwardTimeMs).toReversedEpochMilli();
	}

	@Deprecated // use MilliTime and MilliTimeReversed
	public static Instant fromReverseInstantLong(Long reversedTimeMs){
		if(reversedTimeMs == null){
			return null;
		}
		return MilliTime.ofReversedEpochMilli(reversedTimeMs).toInstant();
	}

}
