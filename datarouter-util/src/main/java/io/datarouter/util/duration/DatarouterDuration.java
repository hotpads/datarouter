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
package io.datarouter.util.duration;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.lang.ClassTool;

public class DatarouterDuration{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterDuration.class);

	public static final DatarouterDuration ZERO = new DatarouterDuration(0, TimeUnit.MILLISECONDS);
	public static final DatarouterDuration MAX_VALUE = new DatarouterDuration(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

	public static final String REGEX =
			"^0$|^(m|M)(a|A)(x|X)$|^((\\d+d)?(\\d+h)?(\\d+m)?(\\d+s)?(\\d+ms)?(\\d+us)?){1,1}$";

	private static final TimeUnit[] TIME_UNITS = {
		TimeUnit.DAYS,
		TimeUnit.HOURS,
		TimeUnit.MINUTES,
		TimeUnit.SECONDS,
		TimeUnit.MILLISECONDS,
		TimeUnit.MICROSECONDS,
	};

	private static final List<String> UNIT_STRINGS = List.of(
		"d",
		"h",
		"m",
		"s",
		"ms",
		"us");

	private long nano;

	public DatarouterDuration(String string) throws IllegalArgumentException{
		if("0".equals(string)){
			nano = 0;
			return;
		}
		string = string.toLowerCase().replaceAll("\\s", "");
		if("max".equals(string)){
			nano = Long.MAX_VALUE;
			return;
		}
		String[] inputValues = string.split("[a-z]+");
		String[] inputUnits = string.split("\\d+");
		for(int i = 0; i < inputValues.length; i++){
			String inputUnit = inputUnits[i + 1];
			int inputUnitIndex = UNIT_STRINGS.indexOf(inputUnit);
			if(inputUnitIndex == -1){
				throw new RuntimeException("unknown unit=" + inputUnit);
			}
			nano += TIME_UNITS[inputUnitIndex].toNanos(Long.parseLong(inputValues[i]));
		}
	}

	public DatarouterDuration(long amount, TimeUnit unit){
		nano = unit.toNanos(amount);
	}

	public DatarouterDuration(Duration duration){
		try{
			nano = duration.toNanos();
		}catch(ArithmeticException e){
			logger.warn("truncating very long duration={}", duration, e);
			nano = Long.MAX_VALUE;
		}
	}

	public long toSecond(){
		return to(TimeUnit.SECONDS);
	}

	public long toMillis(){
		return to(TimeUnit.MILLISECONDS);
	}

	public long to(TimeUnit timeUnit){
		return timeUnit.convert(nano, TimeUnit.NANOSECONDS);
	}

	public Duration toJavaDuration(){
		return Duration.ofNanos(to(TimeUnit.NANOSECONDS));
	}

	public static DatarouterDuration ageMs(long dateMs){
		long ageMs = System.currentTimeMillis() - dateMs;
		return new DatarouterDuration(ageMs, TimeUnit.MILLISECONDS);
	}

	public static DatarouterDuration ageNs(long dateNs){
		long ageNs = System.nanoTime() - dateNs;
		return new DatarouterDuration(ageNs, TimeUnit.NANOSECONDS);
	}

	public static DatarouterDuration age(Date date){
		return ageMs(date.getTime());
	}

	public static DatarouterDuration age(Instant instant){
		return ageMs(instant.toEpochMilli());
	}

	public static DatarouterDuration age(ZonedDateTime zonedDateTime){
		return age(zonedDateTime.toInstant());
	}

	@Override
	public String toString(){
		return toString(TimeUnit.MILLISECONDS);
	}

	public String toString(TimeUnit precision){
		int maxIndex = Arrays.asList(TIME_UNITS).indexOf(precision);
		if(maxIndex == -1){
			maxIndex = TIME_UNITS.length - 1;
		}
		long rest = nano;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < maxIndex + 1; i++){
			long unit = TIME_UNITS[i].toNanos(1);
			long val = rest / unit;
			rest = rest % unit;
			if(val != 0 || i == maxIndex && builder.length() == 0){
				builder.append(val + UNIT_STRINGS.get(i));
			}
		}
		return builder.toString();
	}

	public static boolean isDuration(String string){
		try{
			new DatarouterDuration(string);
		}catch(RuntimeException e){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode(){
		return Long.hashCode(nano);
	}

	@Override
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){
			return false;
		}
		DatarouterDuration other = (DatarouterDuration)obj;
		return nano == other.nano;
	}

	public boolean isLongerThan(DatarouterDuration other){
		return this.nano > other.nano;
	}

	public boolean isShorterThan(DatarouterDuration other){
		return this.nano < other.nano;
	}

}
