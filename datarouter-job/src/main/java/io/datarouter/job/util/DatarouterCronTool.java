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
package io.datarouter.job.util;

import java.time.Duration;
import java.time.LocalTime;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.util.HashMethods;

public class DatarouterCronTool{

	public static String everySecond(){
		return "* * * * * ?";
	}

	public static String everyMinute(String... seeds){
		String seed = String.join("/", seeds);
		return String.format(
				"%s * * * * ?",
				pseudorandomInteger(60, seed + "/seconds"));
	}

	public static String everyHour(String... seeds){
		String seed = String.join("/", seeds);
		return String.format(
				"%s %s * * * ?",
				pseudorandomInteger(60, seed + "/seconds"),
				pseudorandomInteger(60, seed + "/minutes"));
	}

	public static String everyDay(String... seeds){
		String seed = String.join("/", seeds);
		return String.format(
				"%s %s %s * * ?",
				pseudorandomInteger(60, seed + "/seconds"),
				pseudorandomInteger(60, seed + "/minutes"),
				pseudorandomInteger(24, seed + "/hours"));
	}

	public static String everyNSeconds(int seconds, String... seeds){
		String seed = String.join("/", seeds);
		return String.format(
				"%s/%s * * * * ?",
				pseudorandomInteger(seconds, seed + "/seconds"),
				seconds);
	}

	public static String everyNMinutes(int minutes, String... seeds){
		String seed = String.join("/", seeds);
		return String.format(
				"%s %s/%s * * * ?",
				pseudorandomInteger(60, seed + "/seconds"),
				pseudorandomInteger(minutes, seed + "/minutes"),
				minutes);
	}

	public static String everyNHours(int hours, String... seeds){
		String seed = String.join("/", seeds);
		return String.format(
				"%s %s %s/%s * * ?",
				pseudorandomInteger(60, seed + "/seconds"),
				pseudorandomInteger(60, seed + "/minutes"),
				pseudorandomInteger(hours, seed + "/hours"),
				hours);
	}

	public static String everyDayAfter(LocalTime after, Duration windowLength, String... seeds){
		return onDaysOfWeekAfter(DatarouterCronDayOfWeek.allDays(), after, windowLength, seeds);
	}

	public static String onDaysOfWeekAfter(String daysOfWeek, LocalTime after, Duration windowLength, String... seeds){
		String seed = String.join("/", seeds);
		int offsetSeconds = pseudorandomInteger((int)windowLength.getSeconds(), seed);
		LocalTime cronTime = after.plusSeconds(offsetSeconds);
		return String.format(
				"%s %s %s ? * %s",
				cronTime.getSecond(),
				cronTime.getMinute(),
				cronTime.getHour(),
				daysOfWeek);
	}

	private static int pseudorandomInteger(int maxExclusive, String seed){
		byte[] seedBytes = StringCodec.UTF_8.encode(seed);
		int hash = HashMethods.crc32(seedBytes);
		int hashNonNegative = hash & 0x7FFFFFFF;
		return hashNonNegative % maxExclusive;
	}

}
