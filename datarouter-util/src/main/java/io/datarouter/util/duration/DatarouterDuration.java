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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.datarouter.util.lang.ClassTool;

public class DatarouterDuration{

	public static final DatarouterDuration ZERO = new DatarouterDuration(0, TimeUnit.MILLISECONDS);

	public static final DatarouterDuration MAX_VALUE = new DatarouterDuration(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

	public static final String REGEX =
			"^0$|^(m|M)(a|A)(x|X)$|^((\\d+d)?(\\d+h)?(\\d+m)?(\\d+s)?(\\d+ms)?(\\d+us)?){1,1}$";

	private static final TimeUnit[] timeUnits = new TimeUnit[]{
		TimeUnit.DAYS,
		TimeUnit.HOURS,
		TimeUnit.MINUTES,
		TimeUnit.SECONDS,
		TimeUnit.MILLISECONDS,
		TimeUnit.MICROSECONDS,
	};

	private static final String[] strings = new String[]{
		"d",
		"h",
		"m",
		"s",
		"ms",
		"us",
	};

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
		String[] values = string.split("[a-z]+");
		String[] unites = string.split("\\d+");
		List<String> asList = Arrays.asList(strings);
		for(int i = 0; i < values.length; i++){
			nano += timeUnits[asList.indexOf(unites[i + 1])].toNanos(Long.parseLong(values[i]));
		}
	}

	public DatarouterDuration(long amount, TimeUnit unit){
		nano = unit.toNanos(amount);
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

	public java.time.Duration toJavaDuration(){
		return java.time.Duration.ofNanos(to(TimeUnit.NANOSECONDS));
	}

	@Override
	public String toString(){
		return toString(TimeUnit.MILLISECONDS);
	}

	public String toString(TimeUnit precision){
		int maxIndex = Arrays.asList(timeUnits).indexOf(precision);
		if(maxIndex == -1){
			maxIndex = timeUnits.length - 1;
		}
		long rest = nano;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < maxIndex + 1; i++){
			long unit = timeUnits[i].toNanos(1);
			long val = rest / unit;
			rest = rest % unit;
			if(val != 0 || i == maxIndex && builder.length() == 0){
				builder.append(val + strings[i]);
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

}